(ns ether.lib.time
  #?(:clj
     (:import [java.util Timer TimerTask])))

(def animation-frame-ms 16)

#?(:clj
   (defn- timer-task [task]
     (proxy [TimerTask] []
       (run []
         (try
           (task)
           (catch Exception e
             {:error e :task task}))))))

#?(:clj
   (defn run-periodically [task n]
     (let [task  (timer-task task)
           timer (Timer.)]
       (.schedule timer task 0 n)
       timer))
   :cljs
   (defn run-periodically [task n]
     (js/setInterval task n)))

#?(:clj
   (defn clear-timer
     [timer]
     (.cancel timer))
   :cljs
   (defn clear-timer
     [task]
     (js/clearInterval task)))

(defn date
  ([]
   #?(:clj
      (java.util.Date.)
      :cljs
      (js/Date.)))
  ([t]
   #?(:clj
      (java.util.Date. t)
      :cljs
      (js/Date. t))))
