(ns ether.estuary.ui.core
  (:require
   [rum.core :as rum]))

(rum/defc page < rum/reactive
  [*engine location ctor]
  (let [{:engine/keys [state failed-actions]} (rum/react *engine)]
    [:.container.estuary
     ;; TODO: install styles
     [:header.navbar
      [:section.navbar-primary.navbar-section
       [:a.navbar-brand "Estuary"]
       [:a.btn.btn-link "Friends"]
       [:a.btn.btn-link "Feed"]]]
     [:section#page
      [:.container
       (ctor state location)]]]))
