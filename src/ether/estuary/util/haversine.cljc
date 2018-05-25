(ns ether.estuary.util.haversine)

(def R "Earth radius in meters"
  637100)

(defn radians [v]
  #?(:clj  (Math/toRadians v)
     :cljs (.radians js/Math v)))

(defn sin [v]
  #?(:clj (Math/sin v)
     :cljs (.sin js/Math v)))

(defn cos [v]
  #?(:clj (Math/cos v)
     :cljs (.cos js/Math v)))

(defn atan2 [v1 v2]
  #?(:clj (Math/atan2 v1 v2)
     :cljs (.atan2 js/Math v1 v2)))

(defn sqrt [v]
  #?(:clj (Math/sqrt v)
     :cljs (.sqrt js/Math v)))

(defn pow [v1 v2]
  #?(:clj (Math/pow v1 v2)
     :cljs (.pow js/Math v1 v2)))


(defn in-meters [[lng1 lat1] [lng2 lat2]]
  (let [lat-phi1     (radians lat1)
        lat-phi2     (radians lat2)
        delta-phi    (radians (- lat2 lat1))
        delta-lambda (radians (- lng2 lng1))
        a            (+ (pow (sin (/ delta-phi 2)) 2)
                        (* (cos lat-phi1)
                           (cos lat-phi2)
                           (pow (sin (/ delta-lambda 2)) 2)))]
    (* R
       (* (atan2 (sqrt a) (sqrt (- 1.0 a))) 2))))

(defn in-kilometers [coord1 coord2]
  (/ (in-meters coord1 coord2)
     10000))
