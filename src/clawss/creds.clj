(ns clawss.creds
  (:require [clojure.java.io :as io]
            [environ.core :refer [env]]
            [clj-http.conn-mgr :refer (get-keystore)]
            ))

(defn canonical-path "~ doesn't work on windows, use abs path."  [path]
  (and path (.getCanonicalPath (io/file (clojure.string/replace  path #"~" (env :home)))))
  )

(defn get-keystore-registry []
  {:trust-store (canonical-path (env :trust-store))
   :trust-store-pass (env :trust-store-pass)
   :insecure?        (env :insecure?)

   :keystore (canonical-path (env :keystore))
   :keystore-pass (env :keystore-pass)
   :keystore-type (env :keystore-type)})

(defn keystores  [{:keys [keystore keystore-type keystore-pass
                          trust-store trust-store-type trust-store-pass] }]
  {:keystore (get-keystore keystore keystore-type keystore-pass)
   :trust-store (get-keystore trust-store trust-store-type trust-store-pass)})

(defn keystore []
  (:keystore (keystores (get-keystore-registry)))
  )
