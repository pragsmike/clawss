(ns clawss.core
  (:require [ring.util.response]
            [clj-xpath.core :as xp])
  )

(defn to-int [val] (if (number? val) val (Integer/parseInt val)))

(defn split-map
  "Returns a pair of maps.
  First is the subset of map m that contains the keys.
  Second is the remainder of map m.
      (split-map #{:a :b} {:a 1 :b 2 :c 3}) => [{:a 1 :b 2} {:c 3}]
      "
  [keys m]
  (let [[in out] ((juxt filter remove) #(keys (first %)) m)]
    [(into {} in) (into {} out)]
    )
  )

(defn get-content-type [resp]
  (or (get-in resp [:headers "content-type"])
      (name (:content-type resp "application/soap+xml"))
      )
  )

(defn is-soap-response? [resp]
  (and resp
       (re-find #"application/soap\+xml" (get-content-type resp))
       )
  )
