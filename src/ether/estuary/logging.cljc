(ns ether.estuary.logging
  (:require
   [clojure.string :as string]))

(defn cljc-ns [env]
  (or (some-> (:ns env)
              :name)
      (.getName *ns*)))

(defn log-key [env level]
  (str
   "[" (or (some-> (cljc-ns env)
                   str
                   (string/split #"\.")
                   last)
           "default")
   "] "
   (string/upper-case (name level))))

(def *log-levels (atom {:root :info}))

(defn should-log? [ns level]
  (= level
     (get @*log-levels ns :info)))

(defn set-level! [symb level]
  (swap! *log-levels assoc (name symb) level))

(defmacro log [level & args]
  (when (should-log? (cljc-ns &env) level)
    (if (:ns &env) ;; cljs uses &env var
      `(js/console.log ~(log-key &env level) ~@args)
      `(println ~(log-key &env level) ~@args))))

(defmacro info [& args]
  `(ether.estuary.logging/log :info ~@args))

(defmacro warn [& args]
  `(ether.estuary.logging/log :warn ~@args))

(defmacro debug [& args]
  `(ether.estuary.logging/log :debug ~@args))

(defmacro error [& args]
  `(ether.estuary.logging/log :error ~@args))
