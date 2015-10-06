(ns clawss.t-creds
  (:require [clojure.test :refer :all]
            [clawss.creds :as creds]))

(testing "keystores"
  (deftest test-get-keystore-registry
    (is (associative? (creds/get-keystore-registry)))
    )
  (deftest test-keystore
    (is (= java.security.KeyStore (type (creds/keystore))))))
