(ns ether.lib.logging
  (:require [taoensso.timbre :as timbre]
            [clojure.string :as string]))

(defn set-level! [level]
  (timbre/set-level! level))

(defn merge-config! [config]
  (timbre/merge-config! config))

#?(:cljs
   (defn console-set-level! [level-str]
     (timbre/set-level! (keyword level-str))))

(def LOGGING_DATE_FORMAT "yyyy-MM-dd HH:mm:ss.SSS")

(timbre/merge-config!
 {:level     :info
  :output-fn
  (fn [{:keys [level
               ?err
               msg_
               ?ns-str
               ?file]}]
    (string/join " "
                 [#?(:clj (.format (java.text.SimpleDateFormat. LOGGING_DATE_FORMAT)
                                   (java.util.Date.)))
                  (string/upper-case (name level))
                  (or ?ns-str ?file "?")
                  "-"
                  (force msg_)
                  (when-let [err ?err]
                    (str "\n" (timbre/stacktrace err nil)))]))
  :appenders {}
  #_{;; By default, Timbre has the following appender:
              ;; :println (timbre/println-appender {:stream :auto})

     #?@(:clj [;; This is needed for when cider-nrepl steals the default
                        ;; *out* stream, leading to a loss of normal logging
                        ;; when connecting through REPL.
               :println-default-out (assoc (timbre/println-appender)
                                           :fn
                                           (fn [{:keys [output_]}]
                                             (when-not (= *out* default-out)
                                               (binding [*out* default-out]
                                                 (println (force output_))))))])}})

(defmacro trace [& args]
  `(timbre/trace ~@args))

(defmacro debug [& args]
  `(timbre/debug ~@args))

(defmacro info [& args]
  `(timbre/info ~@args))

(defmacro warn [& args]
  `(timbre/warn ~@args))

(defmacro error [& args]
  `(timbre/error ~@args))

(defmacro log [level & args]
  `(case ~level
     :trace (trace ~@args)
     :debug (debug ~@args)
     :info  (info ~@args)
     :warn  (warn ~@args)
     :error (error ~@args)))

#?(:clj (Thread/setDefaultUncaughtExceptionHandler
         (reify Thread$UncaughtExceptionHandler
           (uncaughtException [_ _ t]
             (error t)))))
