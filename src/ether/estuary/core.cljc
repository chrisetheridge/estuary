(ns ether.estuary.core
  #?(:clj
     (:import
      [java.util.UUID])))

(def ^:const DB-URI "datomic:mem://localhost:4334/nearby")
