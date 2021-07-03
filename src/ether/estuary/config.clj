(ns ether.estuary.config
  (:require [clojure.edn :as edn]
            [ether.lib.logging :as logging]
            [mount.core :as mount]))

(defn load-config [path]
  (logging/debug "Loading config from" path)
  (let [{{:keys [build]} :options} (mount/args)]
    (some-> path
            slurp
            edn/read-string
            (assoc-in [:cljs :builds] (mapv keyword build)))))

(mount/defstate config
  :start (load-config "resources/config.edn"))
