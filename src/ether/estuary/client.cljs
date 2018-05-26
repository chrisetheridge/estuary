(ns ether.estuary.client
  (:require [ether.estuary.engine :as engine]
            [ether.estuary.util :as util]
            [ether.estuary.util.transit :as transit]
            [rum.core :as rum])
  (:require-macros [ether.estuary.logging :as logging]))

(enable-console-print!)

(defn did-mount [state]
  (let [[initial-state] (:rum/args state)
        *engine (::*engine state)]
    (reset! *engine (engine/start-engine initial-state))
    #_(engine/perform-action! *engine {:action/key    ::start
                                     :event/started (util/inst-ms)})
    (assoc state ::*engine *engine)))

(defmethod engine/action ::start [state action]
  (logging/info "action " ::start)
  (assoc state ::started? (:event/started action)))

(rum/defc app < rum/reactive
  [*engine]
  (let [engine (rum/react *engine)]
    [:.container.application {:key (util/react-key ::app)}
     [:.columns
      [:.column.col-8
       [:h1 "Estuary"]]
      [:.column.col-4
       (when engine
         [:.main
          [:h3 "debug state"]
          [:pre (pr-str (:engine/state engine))]])]]]))

(defn start-location-listener! []
  (if-let [geo (.-geolocation js/navigator)]
    (.getCurrentPosition geo
                         (fn [pos]
                           (let [coords     (.-coords pos)
                                 pos-parsed {:lat (.-latitude coords)
                                             :lng (.-longitude coords)}]
                             ))
                         (fn [err]
                           (logging/info "Geolocation error" {:error err})))))



(defn ^:export reload! []
  (logging/info "Reloading client"))

(defn ^:export start! []
  (logging/info "Starting client")
  (let [state-element (js/document.getElementById "init")
        state (transit/read-transit-str (.-textContent state-element))]
    (logging/info ::state state)
    (rum/mount (app (engine/start-engine state)) (js/document.getElementById "app"))))
