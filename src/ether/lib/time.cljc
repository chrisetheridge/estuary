(ns ether.lib.time)

(defn inst []
  #?(:clj
     (java.util.Time.)
     :cljs
     (js/Date.)))

(defn inst-ms
  ([]
   #?(:clj
      (.getTime (java.util.Date.))
      :cljs
      (.getTime (js/Date.))))
  ([t]
   (.getTime t)))
