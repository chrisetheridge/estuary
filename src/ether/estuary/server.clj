(ns ether.estuary.server
  (:gen-class)
  (:require
   [ether.estuary.db :as db]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [ether.estuary.logging :as logging]
   [compojure.core :as cj]
   [compojure.route :as route]
   [immutant.web :as web]
   [ether.estuary.util.transit :as transit]
   [ring.middleware.params :as middleware.params]
   [datomic.api :as d]))

(defn render-index [request template]
  (str/replace template #"\#data\#"
               (transit/write-transit-str {:estuary/ws-uri  "/ws"
                                           :estuary/clients []})))

(defn index [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->> (io/file "resources/public/index.html")
                 (slurp)
                 (render-index request))})

(defn wrap-with-env [handler]
  (let [db-env (db/start-system!)]
    (fn
      ([req]
       (handler (assoc req :estuary/env db-env)))
      ([request respond raise]
       (handler (assoc request :estuary/env db-env)
                #(respond %) raise)))))

(defn log-request [request]
  (let [{:keys [request-method uri]} request]
    (when-not (re-find #"^/public/.*" uri)
      (logging/info (str/upper-case (name request-method))
                    "→"
                    uri))))

(defn wrap-with-logging [handler]
  (fn
    ([req]
     (log-request req)
     (handler req))
    ([req respond raise]
     (log-request req)
     (handler req #(respond %) raise))))

(def web-app
  (-> (cj/routes
       (cj/GET "/" request (index request))
       (route/files "/public" {:root "resources/public"}))
      middleware.params/wrap-params
      wrap-with-env
      wrap-with-logging))

(defonce ^:dynamic *system (atom nil))

(defn start-system! [port]
  (if (:system/running? @*system)
    (logging/info "Server already running")
    (let [port (or port 8080)]
      (logging/info "Starting server on" port)
      (swap! *system merge {:system/web-process (web/run #'web-app {:port port})
                            :system/running?    true}))))

(defn stop! []
  (if-let [stop-args (:system/web-process @*system)]
    (do
      (logging/info "Shutting down server")
      (web/stop stop-args)
      (swap! *system merge {:system/web-process nil
                            :system/running?    false} )
      (logging/info "Server shut down"))
    (logging/info "Server not running")))

(defn restart! [web-port]
  (logging/info "Restarting server")
  (stop!)
  (start-system! web-port)
  (logging/info "Done"))

(defn -main [& [web-port]]
  (start-system! (Integer/parseInt web-port)))

(comment

  (do
    (require '[clojure.tools.namespace.repl])
    (clojure.tools.namespace.repl/refresh)
    (restart! 8080))
  (stop!)
  (start! 8080)

  )
