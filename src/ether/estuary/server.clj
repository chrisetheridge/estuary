(ns ether.estuary.server
  (:require [mount.core :as mount]
            [clojure.string :as string]
            [ether.lib.logging :as logging]
            [ether.lib.transit :as transit]
            [clojure.java.io :as io]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [ring.middleware.params :as middleware.params]
            [org.httpkit.server :as server]
            [ether.estuary.config :as config]))

(defn render-index [_request template]
  (string/replace template #"\#data\#"
                  (transit/write-str {:estuary/ws-uri  "/ws"
                                      :estuary/clients []})))

(defn index [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->> (io/file "resources/public/index.html")
                 (slurp)
                 (render-index request))})

(defn log-request [request]
  (let [{:keys [request-method uri]} request]
    (when-not (re-find #"^/public/.*" uri)
      (logging/debug (string/upper-case (name request-method))
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
  (-> (compojure/routes
       (compojure/GET "/e" request (index request))
       ;; estuary.ws/routes
       (route/files "/public" {:root "resources/public"}))
      middleware.params/wrap-params
      ;; wrap-with-env
      wrap-with-logging))

(defn start-server [{:keys [web]}]
  (let [port (or (:port web) 8080)]
    (logging/info "Starting server on" port)
    (server/run-server #'web-app {:port port})))

(defn stop-server [srv]
  (logging/info "Server:" srv)
  (server/server-stop! srv))

(mount/defstate server
  :start (start-server config/config)
  :stop (stop-server server))
