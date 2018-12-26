(ns ether.estuary.engine
  (:require [clojure.core.async :as async]
            [ether.estuary.logging :as logging]
            [ether.estuary.util :as util]))

;;; Listeners

(defn add-listner! [*engine listener-key callback]
  (swap! *engine update
         :engine/listeners conj [listener-key callback]))

;;; Performing action

(defmulti action
  (fn [state action]
    (:action/key action)))

(defn perform-action! [*engine action]
  (when-let [channel (:engine/action-channel @*engine)]
    (async/put! channel action)))

;;; Engine / system

(defonce *system (atom nil))

(defn stop-system! []
  (swap! *system merge {:system/engine   nil
                        :system/running? false}))

(defn tag-event [event]
  (assoc event :event/client-ts (util/inst-ms)))

(defn- -safe-action [*engine state the-action]
  (try
    (let [after-state (action state the-action)]
      (swap! *engine update :engine/confirmed-actions the-action)
      after-state)
    (catch #?(:clj Exception :cljs js/Error) e
      (do
        (logging/error "Error handling action" {:action action :error e})
        (swap! *engine update :engine/failed-actions
               conj (assoc the-action :action/error e))
        state))))

(defn- -handle-action! [*engine the-action]
  (logging/info "Handling action" the-action)
  (let [engine                           @*engine
        {:engine/keys [listeners state]} engine
        after                            (-safe-action *engine state the-action)]
    (swap! *engine update :engine/state
           (fn [state]
             (if (empty? after)
               (do
                 (logging/error "Empty state after handling action" {:action the-action :state state})
                 state)
               (do
                 (logging/info "After state" after)
                 after))))
    (doseq [[k cb] listeners]
      (logging/info "Calling engine listener" (str k) {:action the-action})
      (cb after the-action))))

(defn base-engine [state channel]
  {:engine/state          state
   :engine/phase          [:engine.phase/none :engine.phase/started]
   :engine/listeners      []
   :engine/action-channel channel
   :engine/meta           {:meta/start          (util/inst)
                           :meta/starting-state state}})

(defn new-engine! [state channel]
  (let [*engine (atom (base-engine state channel))
        loop    (async/go
                  (loop [action (async/<! channel)]
                    (when action
                      (-handle-action! *engine action))
                    (recur (async/<! channel))))]
    (swap! *engine assoc :engine/loop loop)
    *engine))

(defn start-engine [state]
  (let [action-channel (async/chan 25 (map tag-event))]
    (new-engine! state action-channel)))

(defn start-system! [initial-state]
  (swap! *system merge {:system/engine   (start-engine initial-state)
                        :system/running? true}))

(defn stop-engine! [*engine]
  (swap! *engine assoc  :engine/phase [:engine.phase/running :engine.phase/stopped])
  (swap! *engine update :engine/action-channel async/close!)
  (swap! *engine update :engine/meta assoc :meta/stop (util/inst))
  (swap! *engine dissoc :engine/loop))
