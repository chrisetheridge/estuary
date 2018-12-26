(ns ether.estuary.ws
  (:require
   [ether.estuary.db :as db]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [ether.estuary.logging :as logging]
   [ether.estuary.util :as estuary.util]
   [compojure.core :as cj]
   [compojure.route :as route]
   [immutant.web :as web]
   [immutant.web.async :as web.async]
   [ether.estuary.util.transit :as transit]
   [ring.middleware.params :as middleware.params]
   [datomic.api :as d]))

(defonce ^:dynamic *clients (atom nil))
(defonce *messages (atom []))

(defn- find-channel [channel]
  (some->> (seq @*clients)
           (filter #(= (::channel %) channel))
           first))

(defn- on-message-impl [channel message]
  (logging/info "Client message" {:message message})
  (swap! *messages conj message))

(defn- on-open-impl [channel handshake]
  (let [uuid       (estuary.util/new-uuid)
        new-client {::uuid    uuid
                    ::channel channel
                    ::meta    {:start        (estuary.util/inst)
                               :ws-handshake handshake}}]
    (logging/info "New client" new-client)
    (swap! *clients conj new-client)))

(defn- on-close-impl [channel {:keys [code reason] :as data}]
  (logging/warn "Client closed" {:code code :reason reason})
  (swap! *clients remove #(= (::channel %) channel)))

(defn- on-error-impl [channel throwable]
  (logging/error "Error with websocket channel"
                 {:channel-info (find-channel channel)
                  :ex           throwable}))

(defn -handler [req lat lng]
  (-> {:on-message on-message-impl
       :on-open    on-open-impl
       :on-close   on-close-impl
       :on-error   on-error-impl}
      (web.async/as-channel req)))

(def routes
  (cj/routes
   (cj/GET "/ws" [latitude longitude :as request]
     (-handler request latitude longitude))))
