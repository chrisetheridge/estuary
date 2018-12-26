(ns ether.estuary.util
  (:require [clojure.string :as string])
  #?(:cljs (:refer-clojure :exclude [inst-ms])))

(defn inst
  ([]
   #?(:clj
      (java.util.Date.)
      :cljs
      (js/Date.)))
  ([date]
   #?(:clj
      (java.util.Date. date)
      :cljs
      (js/Date. date))))

(defn inst-ms
  ([]
   (.getTime (inst)))
  ([date]
   (.getTime date)))

(defn new-uuid []
  #?(:clj
     (java.util.UUID/randomUUID)
     :cljs
     (random-uuid)))

(defn react-key [comp-key]
  (str "ether_estuary_component/"
       (string/replace (str comp-key) #"\:" "")))

(defn some-map
  "Like clojure.core/hash-map or usual map ctor ({ kv pairs} ),
  only keeps non-nil values."
  [& kvs]
  (assert (== 0 (mod (count kvs) 2))
          "some-map requires even an number of arguments, like clojure.core/hash-map")
  (reduce (fn [m [k v]]
            (if (some? v) (assoc m k v) m))
          {}
          (partition 2 kvs)))
