(ns ether.estuary.client
  (:require [clojure.edn :as edn]))

(defn start! [env-str]
  (let [env (edn/read-string env-str)]))

(defn reload! [])
