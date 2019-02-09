(ns ether.estuary.ui.pages.home
  (:require
   [ether.estuary.engine :as estuary.engine]
   [ether.estuary.ws :as estuary.ws]
   [rum.core :as rum]
   [ether.lib.logging :as logging]))

(defn start-websocket! [state {:keys [latitude longitude]}]
  (let [ws (estuary.ws/connect! state latitude longitude)]
    (logging/info "Client ws" ws)
    ws))

(defmethod estuary.engine/action ::setup-websocket-client [state {:client/keys [position]}]
  (logging/info position position)
  (let [ws (start-websocket! state position)]
    (-> (assoc-in state [:client :client/position] position)
        (assoc-in [:client :client/websocket] ws))))

(defn start-location-listener! []
  (logging/info "Starting location listener")
  (if-let [geo (.-geolocation js/navigator)]
    (.getCurrentPosition geo
                         (fn [pos]
                           (let [coords     (.-coords pos)
                                 pos-parsed {:latitude  (.-latitude coords)
                                             :longitude (.-longitude coords)}]
                             (estuary.engine/dispatch! {:action/key      ::setup-websocket-client
                                                        :client/position pos-parsed})))
                         (fn [err]
                           (logging/info "Geolocation error" {:error err})))
    (logging/error "No geolocation present")))

(rum/defc component [state loc]
  [:.page.page__home
   [:button
    {:on-click
     (fn [e]
       (start-location-listener!))}
    "Start"]])
