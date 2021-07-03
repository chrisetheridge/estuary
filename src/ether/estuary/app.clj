(ns ether.estuary.app
  (:require [cider.nrepl :as cider]
            [clojure.java.io :as io]
            [clojure.tools.cli :as tools.cli]
            [ether.estuary.config :as config]
            [ether.estuary.server]
            [ether.estuary.cljs]
            [ether.lib.logging :as logging]
            [mount.core :as mount]
            [nrepl.server :as nrepl.server]
            [refactor-nrepl.middleware :as refactor]))

(defn clean-up! []
  (io/delete-file (io/file ".nrepl-port") true))

(defn start-repl [{:keys [nrepl]}]
  (let [repl-port (or (:port nrepl) 6661)]
    ;; start repl
    (logging/debug "Starting repl" {:port repl-port})
    (nrepl.server/start-server
     :port    repl-port
     :handler (refactor/wrap-refactor cider/cider-nrepl-handler))
    (spit ".nrepl-port" repl-port)
    (logging/debug "Done repl.")
    ;; clean up hook
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(clean-up!)))))

(mount/defstate repl
  :start (start-repl config/config))

(def SUPPORTED_OPTS
  [["-b" "--build ID" "CLJS build identifier"
    :multi true
    :update-fn conj]])

(defn -main [& args]
  (mount/start-with-args (tools.cli/parse-opts args SUPPORTED_OPTS)))
