(ns ether.estuary.client
  (:require
   [ether.estuary.engine :as estuary.engine]
   [ether.estuary.routes :as estuary.routes]
   [ether.lib.transit :as transit]
   [ether.lib.logging :as logging]))

(enable-console-print!)

(defmethod estuary.engine/action ::start [state action]
  (logging/info "::start" action)
  (assoc state ::started? true))

(defn ^:export reload! []
  (logging/info "Reloading client")
  (estuary.routes/refresh! estuary.engine/*main-engine*))

(defn start-engine! [state]
  (let [*engine (estuary.engine/start-engine state)]
    (estuary.engine/dispatch! {:action/key ::start})
    *engine))

(defn ^:export start! []
  (logging/info "Starting client")
  (let [state-element (js/document.getElementById "e_init")
        state         (transit/read-str (.-textContent state-element))
        *engine       (start-engine! state)]
    (logging/info ::state state)
    (estuary.routes/refresh! *engine)))
