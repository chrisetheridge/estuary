(ns ether.estuary.engine
  (:require [clojure.core.async :as async]
            [ether.lib.time :as time]
            [ether.lib.logging :as logging]
            [datascript.core :as d]))

(defonce *main-loop* (atom nil))

(defn add-listener! [*loop listener-key event callback]
  (swap! *loop assoc-in [:listeners event listener-key] callback))

(defmulti event->tx
  (fn [_state {:keys [event]}]
    event))

(defn dispatch! [e]
  (when-let [channel (:action-channel @*main-loop*)]
    (async/put! channel e)))

(defn enrich-event [e]
  (assoc e :client-ts (time/inst-ms)))

(defn safe-action [*loop state event]
  (try
    (let [tx    (event->tx state event)
          after (d/with state tx)]
      (swap! *loop update :confirmed-events conj event)
      after)
    (catch #?(:clj Exception :cljs js/Error) e
      (logging/error "Error handling event" event)
      (swap! *loop update :failed-events conj event)
      state)))

(defn handle-event [*loop event]
  (logging/debug "Handling event" (:event event))
  (let [{:keys [listeners state]} @*loop
        event                     (enrich-event event)
        db-after                  (safe-action *loop state event)]
    (swap! *loop assoc :state db-after)))

;; Event sourcing with datomic/datascript db as state
;; Bi directional websocket sync
;; Receive events from server and send back to server
