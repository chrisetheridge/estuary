(ns ether.estuary.ws
  (:require
   [ether.estuary.logging :as logging]
   [ether.estuary.util.transit :as estuary.transit]
   [ether.estuary.util :as estuary.util]
   [ether.estuary.engine :as estuary.engine]))

(defonce ^:dynamic *clients (atom []))

(defn parse-url [{:estuary/keys [ws-uri]} lng lat]
  (str "/" ws-uri "?latitude=" lat "&longitude=" lng))

(defn connect! [*engine lng lat]
  (let [{:keys [state]} @*engine
        url             (parse-url state lng lat)
        socket          (js/Websocket. url)
        client-uuid     (estuary.util/new-uuid)]
    (logging/info "Connecting new WS client" lng lat)
    (set! (.-onmessage socket)
          (fn [e]
            (let [data (estuary.transit/read-str (aget e "data"))]
              (->> (estuary.util/some-map
                    :action/key (:action data)
                    :action/data (dissoc data :action))
                   (estuary.engine/perform-action! *engine)))))
    (set! (.-onerror socket)
          (fn [e]
            (logging/error "WS client error" {:ex e})))
    (set! (.-onclose socket)
          (fn [reason]
            (logging/warn "WS client closed" {:reason reason
                                              :socket socket})))
    (set! (.-onopen socket)
          (fn [d]
            (logging/info "WS client open" d)
            (swap! *clients conj {:socket   socket
                                  :uuid     client-uuid})))))
