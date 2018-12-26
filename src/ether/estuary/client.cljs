(ns ether.estuary.client
  (:require
   [ether.estuary.engine :as estuary.engine]
   [ether.estuary.util :as estuary.util]
   [ether.estuary.util.transit :as transit]
   [ether.estuary.ws :as estuary.ws]
   [rum.core :as rum])
  (:require-macros
   [ether.estuary.logging :as logging]))

(enable-console-print!)

(defmethod estuary.engine/action ::start [state action]
  (logging/info "::start" action)
  (assoc state ::started? true))

(defn start-websocket! [state {:keys [latitude longitude]}]
  (let [ws (estuary.ws/connect! state latitude longitude)]
    (logging/info "Client ws" ws)
    ws))

(defmethod estuary.engine/action ::setup-websocket-client [state {:client/keys [position]}]
  (logging/info position position)
  (let [ws (start-websocket! state position)]
    (-> (assoc-in state [:client :client/position] position)
        (assoc-in [:client :client/websocket] ws))))

(defn start-location-listener! [*engine]
  (if-let [geo (.-geolocation js/navigator)]
    (.getCurrentPosition geo
                         (fn [pos]
                           (let [coords     (.-coords pos)
                                 pos-parsed {:latitude  (.-latitude coords)
                                             :longitude (.-longitude coords)}]
                             (->> (estuary.util/some-map
                                   :action/key      ::setup-websocket-client
                                   :client/position pos-parsed)
                                  (estuary.engine/perform-action! *engine))))
                         (fn [err]
                           (logging/info "Geolocation error" {:error err})))
    (logging/error "No geolocation present")))

(defn submit-form! [*engine form]
  (logging/info ::form form)
  (start-location-listener! *engine))

(rum/defcs login-form < (rum/local {} ::form)
  [{::keys [form]} *engine]
  [:.login.form
   [:form
    [:.form-group
     [:label.form-label "Email"]
     [:input.form-input
      {:type        "email"
       :placeholder "audiophile@gmail.com"}]]
    [:.form-group
     [:label.form-label "Nickname"]
     [:input.form-input
      {:type        "text"
       :placeholder "audiophile21"}]]
    [:.form-group
     [:button.btn
      {:type     "button"
       :on-click #(submit-form! *engine form)}
      "Login"]]]])

(rum/defc app < rum/reactive
  [*engine]
  (let [{:engine/keys [state failed-actions]} (rum/react *engine)]
    [:.container.application {:key (estuary.util/react-key ::app)}
     [:.columns
      [:.column.col-8
       [:h1 "Estuary"]
       (when (::started? state)
         (list
          [:.container
           (login-form *engine)]
          [:.container
           (when-let [{:keys [lat lng]} (:client/position state)]
             [:h3 "Positon"]
             [:p.primary (str "Lat: " lat ", Lng: " lng)])]))]
      [:.column.col-4
       [:h1 "Debug"]
       [:.container
        [:h3 "Error actions"]
        [:.container.errors
         (for [action failed-actions]
           [:pre (pr-str action)])]]]]]))

(defn ^:export reload! []
  (logging/info "Reloading client"))

(defn start-engine! [state]
  (let [*engine (estuary.engine/start-engine state)]
    (estuary.engine/perform-action! *engine {:action/key ::start})
    *engine))


(defn ^:export start! []
  (logging/info "Starting client")
  (let [state-element (js/document.getElementById "init")
        state         (transit/read-str (.-textContent state-element))]
    (logging/info ::state state)
    (rum/mount (app (start-engine! state)) (js/document.getElementById "app"))))
