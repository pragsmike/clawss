(ns clawss.t-saml
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [clj-xpath.core :as xp]
            [clawss.saml :as saml]))

(def saml-props {:id "d2"
                 :authn-instant "2011-12-05T17:55:45Z"
                 :issue-instant "2011-12-05T17:55:45Z"
                 :not-before "2011-12-05T16:56:07Z"
                 :not-on-or-after "2011-12-05T18:56:07Z"
                 :nameid-format "urn:some.id.format"
                 :nameid "joe"
                 })

(deftest test-get-saml-props
  (is (=
       (saml/get-saml-props "some.type" "joe" "some.uuid" (t/date-time 1970))
       {:nameid-format "some.type"
        :nameid "joe"
        :id "some.uuid"
        :authn-instant "1970-01-01T00:00:00Z",
        :issue-instant "1970-01-01T00:00:00Z",
        :not-before "1969-12-31T23:59:00Z",
        :not-on-or-after "1970-01-01T01:00:00Z"})))

(deftest test-get-saml-assertion
  (let [saml-doc (saml/get-saml-assertion saml-props)
        saml (.getDocumentElement saml-doc)]
    (is (= saml/NS-SAML (.getNamespaceURI saml)))
    (is (= (:id saml-props) (xp/$x:text "@ID" saml)))
    (is (= "2011-12-05T17:55:45Z" (xp/$x:text "@IssueInstant" saml)))
    (is (= "2011-12-05T16:56:07Z"    (xp/$x:text "//@NotBefore" saml)))
    (is (= "2011-12-05T18:56:07Z"    (xp/$x:text "//@NotOnOrAfter" saml)))
    (is (= "2011-12-05T17:55:45Z" (xp/$x:text "//@AuthnInstant" saml)))
    (is (= "urn:some.id.format" (xp/$x:text "//*[local-name()='Subject']//@Format" saml)))
    (is (= "joe"   (xp/$x:text "*/*[local-name()='NameID']" saml)))))
