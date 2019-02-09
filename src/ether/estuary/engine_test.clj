(ns ether.estuary.engine-test
  (:require
   [ether.estuary.engine :as engine]
   [clojure.test :as test :refer [is deftest]]
   [clojure.core.async :as async]
   [ether.lib.logging :as logging]))

(deftest basic-engine
  (let [channel     (async/chan 25)
        state       {:x (take (rand-int 10)
                              (shuffle (range 10000)))}
        *new-engine (engine/new-engine! state channel)
        engine      (deref *new-engine)]
    (logging/info "Testing base engine state" {:test/state state})
    (is (= (:engine/state engine) state))
    (is (= (:engine/listeners engine) []))
    (is (= (:engine/action-channel engine) channel))
    (is (= (get-in engine [:engine/meta :meta/starting-state])
           state))
    (is (some? (:engine/loop engine)))
    (is (= (:engine/phase engine) [:engine.phase/none :engine.phase/started]))

    ;; stop engine
    (engine/stop-engine! *new-engine)

    (let [engine @*new-engine]
      (is (:engine/phase engine) [:engine.phase/running :engine.phase/stopped])
      (is (nil? (:engine/loop engine)))
      (is (nil? (:engine/action-channel engine))))))
