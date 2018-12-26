(ns ether.estuary.util.transit
  (:require [cognitect.transit :as t]))

(defn read-str [s]
  (t/read (t/reader :json) s))

(defn write-str [o]
  (t/write (t/writer :json) o))
