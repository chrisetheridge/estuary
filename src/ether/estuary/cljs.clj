(ns ether.estuary.cljs
  (:require [ether.estuary.config :as config]
            [mount.core :as mount]
            [ether.lib.logging :as logging]
            [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]))

(defn start-cljs-builds [{{:keys [builds]} :cljs}]
  (logging/debug "Starting cljs builds" builds)
  (shadow.server/start!)
  (doseq [build builds]
    (logging/debug "Starting cljs watcher for" build)
    (shadow.api/watch build)))

(defn stop-cljs [{{:keys [builds]} :cljs}]
  (logging/debug "Stopping cljs builds" builds)
  (doseq [build builds]
    (shadow.api/stop-worker build))
  (shadow.server/stop!))

(mount/defstate compile-cljs
  :start (start-cljs-builds config/config)
  :stop (stop-cljs config/config))
