(ns ether.lib.url
  (:require
    [#?(:cljs cljs.core :clj clojure.core) :as core]
    [clojure.string :as str]))

(defn decode-uri-component [s]
  #?(:cljs (js/decodeURIComponent s)
     :clj  (java.net.URLDecoder/decode s "UTF-8")))


(defn encode-uri-component [s]
  #?(:cljs (js/encodeURIComponent s)
     :clj  (-> s
               (java.net.URLEncoder/encode "UTF-8")
               (str/replace #"\+"   "%20")
               (str/replace #"\%21" "!")
               (str/replace #"\%27" "'")
               (str/replace #"\%28" "(")
               (str/replace #"\%29" ")")
               (str/replace #"\%7E" "~"))))


(defn parse-query [s]
  (->> (str/split s #"&")
       (map (fn [s] (let [[k v] (str/split s #"=" 2)]
                      [(keyword (decode-uri-component k))
                       (when-not (str/blank? v) (decode-uri-component v))])))
       (reduce (fn [m [k v]]
                 (if (contains? m k)
                   (let [old-v (get m k)]
                     (if (vector? old-v)
                       (core/assoc m k (conj old-v v))
                       (core/assoc m k [old-v v])))
                   (core/assoc m k v))) {})))


(defn parse [s]
  (let [[_ l _ q _ f] (re-matches #"([^?#]*)(\?([^#]*))?(\#(.*))?" s)
        [_ s d p]     (re-matches #"([a-zA-Z]+):///?([^/]+)(/.*)"  l)]
    (cond-> {:location l
             :path     p
             :scheme   s
             :domain   d
             :fragment f}
      q (assoc :query (parse-query q)))))
