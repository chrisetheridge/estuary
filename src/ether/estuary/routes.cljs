(ns ether.estuary.routes
  (:require
   [ether.lib.ui.routing :as ui.routing]
   [ether.estuary.ui.core :as ui.core]
   [ether.estuary.ui.pages.home :as ui.pages.home]))

(def routes
  [
   [::home "/e" ui.pages.home/component]
   ])

(defn refresh! [*engine]
  (ui.routing/clear-routes!)
  (doseq [[route-key pattern ctor] routes]
    (ui.routing/add-route! route-key {:match (re-pattern pattern)
                                      :ctor  #(ui.core/page *engine % ctor)}))
  (ui.routing/refresh))
