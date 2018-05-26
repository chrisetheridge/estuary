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
