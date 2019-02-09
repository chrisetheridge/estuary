(ns ether.estuary.db
  (:require
   [datomic.api :as d]
   [ether.estuary.core :as core]))

(defn conn [] (d/connect core/DB-URI))

(defn db [] (d/db (conn)))

(defonce ^:dynamic *system (atom nil))

(defn start-system! []
  (try
    (let [conn (d/connect core/DB-URI)]
      (reset! *system {:datomic/conn    conn
                       :system/started  (java.util.Date.)
                       :system/running? true
                       :system/name     :nearby.system/datomic}))
    (catch Exception e
      (d/create-database core/DB-URI)
      (start-system!))))

(defn stop-system! []
  (d/release (:datomic/conn @*system))
  (swap! *system dissoc :datomic/connn))
