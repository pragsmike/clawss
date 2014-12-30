(ns clawss.t-creds
  (:require [midje.sweet :refer :all]
            [clawss.creds :as creds]))

(facts "keystores"
       (fact "get-keystore-registry"
             (creds/get-keystore-registry) => associative?
             )
       (fact "keystore"
             (type (creds/keystore)) =>  java.security.KeyStore )
       )
