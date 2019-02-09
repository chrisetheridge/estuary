(ns ether.lib.transit
  (:require
   [cognitect.transit :as t])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn read-transit [is]
  (t/read (t/reader is :json)))

(defn read-str [^String s]
  (read-transit (ByteArrayInputStream. (.getBytes s "UTF-8"))))

(defn write-transit [o os]
  (t/write (t/writer os :json) o))

(defn write-transit-bytes ^bytes [o]
  (let [os (ByteArrayOutputStream.)]
    (write-transit o os)
    (.toByteArray os)))

(defn write-str [o]
  (String. (write-transit-bytes o) "UTF-8"))
