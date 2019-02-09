(ns ether.lib.logging
  (:require
   [clojure.string :as string]))

(defn cljc-ns [env]
  (or (some-> (:ns env)
              :name)
      (.getName *ns*)))

(defn parse-ns [ns-str]
  (prn :q? (re-find #"\.ui\." ns-str))
  (if (re-find #"\.ui\." ns-str)
    (->> (string/split ns-str #"\.")
         (drop-while #(not= % "ui")))
    ns-str))

(defn log-key [env level]
  (str
   "[" (or (some-> (cljc-ns env)
                   str
                   (parse-ns))
           "?ns")
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
    (if (:ns &env) ;; cljs uses :ns &env var
      `(js/console.log ~(log-key &env level) (apply str ~@args))
      `(println ~(log-key &env level) (apply str ~@args)))))

(defmacro info [& args]
  `(ether.lib.logging/log :info ~@args))

(defmacro warn [& args]
  `(ether.lib.logging/log :warn ~@args))

(defmacro debug [& args]
  `(ether.lib.logging/log :debug ~@args))

(defmacro error [& args]
  `(ether.lib.logging/log :error ~@args))
