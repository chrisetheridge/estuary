(ns ether.repl
  (:require
   [cider.nrepl :as cider]
   [clojure.java.io :as io]
   [clojure.tools.nrepl.server :as nrepl.server]
   [ether.lib.logging :as logging]
   [refactor-nrepl.middleware :as refactor]
   [shadow.cljs.devtools.api :as shadow.api]
   [shadow.cljs.devtools.server :as shadow.server]))

(defn clean-up! [cljs-build]
  (logging/debug "Starting cljs clean-up!" {:build cljs-build})
  (-> (io/file ".nrepl-port")
      (io/delete-file true))
  ;; stop cljs build
  (shadow.api/stop-worker cljs-build)
  ;; stop cljs server
  (shadow.server/stop!)
  (logging/debug "Done clean-up!" {:build cljs-build}))

(defn -main [& [repl-port web-port cljs-build]]
  (let [cljs-build (or (some-> cljs-build keyword) :dev)
        repl-port  (or (some-> repl-port Integer/parseInt) 6661)
        ;; web-port   (or (some-> web-port Integer/parseInt) 8080)
        ]
    ;; start repl
    (logging/debug "Starting repl" {:port repl-port})
    (nrepl.server/start-server
     :port    repl-port
     :handler (refactor/wrap-refactor cider/cider-nrepl-handler))
    (spit ".nrepl-port" repl-port)
    (logging/debug "Done repl.")

    ;; start cljs
    (logging/debug "Starting cljs" {:build cljs-build})
    (shadow.server/start!)
    (shadow.api/watch cljs-build)
    (logging/debug "Done cljs" {:build cljs-build})

    ;; start server
    ;; TODO

    ;; clean up hook
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(clean-up! cljs-build)))))
