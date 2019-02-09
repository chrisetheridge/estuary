(ns ether.lib.ui.routing
  (:require
   [goog.userAgent :as ua]
   [clojure.string :as str]
   [ether.lib.url :as url]
   [rum.core :as rum]))

(defonce ^:private *routes (atom {}))

(defonce *route (atom nil))

(defn location []
  js/document.location)

(defn path
  ([] (path (location)))
  ([loc] (.-pathname loc)))

(defonce *path (atom (path)))

(defn add-route!
  ":match, :ctor"
  [key opts]
  (swap! *routes assoc key (assoc opts :key key)))

(defn- match? [route loc]
  (let [m (:match route)]
    (cond
      (regexp? m) (re-matches m (path loc))
      (fn? m)     (m loc)
      (string? m) (= m (path loc))
      :else       (throw (ex-info "unknown matcher" m)))))

(defn- current-route []
  (let [loc (location)]
    (or
     (second (first (filter (fn [[_k o]] (match? o loc)) @*routes)))
     (throw (ex-info (str "No registered route found for " (.-href loc))
                     {:href (.-href loc)})))))

(defn render-route []
  (let [{:keys [key ctor] :as route} (current-route)
        _                            (reset! *route key)
        loc                          (location)
        path                         (path loc)
        _                            (reset! *path path)]
    (if (and ctor key)
      (do
        (rum/mount (ctor loc) (js/document.getElementById "react_mount"))
        route)
      (throw (ex-info "Error rendering route" {:current current-route})))))

(defn- relative? [url]
  (let [{:keys [location domain]} (url/parse url)]
    (and (not (str/starts-with? location "/api/"))
         (or (nil? domain)
             (= domain js/location.host)))))

;; from jQuery .which method
;; https://code.jquery.com/jquery-3.1.0.js
;; // Add which for click: 1 === left; 2 === middle; 3 === right
;; if ( !event.which && button !== undefined && rmouseEvent.test( event.type ) ) {
;;   return ( button & 1 ? 1 : ( button & 2 ? 3 : ( button & 4 ? 2 : 0 ) ) );
;; }
(defn- mouse-button [e]
  (or (.-which e)
      (when-let [b (.-button e)]
        (cond
          (pos? (bit-and b 1)) 1
          (pos? (bit-and b 2)) 3
          (pos? (bit-and b 4)) 2
          :else                0))))

(defn- force-new-window? [e]
  (or (if ua/MAC
        (.-metaKey e)
        (.-ctrlKey e))
      (== 2 (mouse-button e))))

(defn- navigate!
  ([navigate-fn url]
   (if (and (relative? url) navigate-fn)
     (do
       (.call navigate-fn js/history nil "" url)
       (render-route))
     (try
       (set! js/window.location.href url)
       (catch :default e)))) ;; expected exception from IE if beforeunload got cancelled
  ([navigate-fn url event]
   (.preventDefault event)
   (if (force-new-window? event)
     (js/window.open url)
     (navigate! navigate-fn url))))

(defn go!
  ([url] (navigate! js/history.pushState url))
  ([url event] (navigate! js/history.pushState url event)))

(defn reload!
  ([url] (navigate! js/history.replaceState url))
  ([url event] (navigate! js/history.replaceState url event)))

(defn refresh []
  (set! js/onpopstate render-route)
  (render-route))

(defn clear-routes! []
  (reset! *routes {}))
