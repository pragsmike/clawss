(ns clawss.t-saml
  (:require [midje.sweet :refer :all]
            [saacl.xml :as xml]
            [saacl.soap :as soap]
            [clj-xpath.core :as xp]
            [clojure.java.io :as io]
            [clawss.saml :as saml])
  (:import (org.joda.time DateTime)))

(fact "get-saml-props"
      (saml/get-saml-props "some.type" "joe" "some.uuid" (DateTime. 0))
      => {:nameid-format "some.type"
          :nameid "joe"
          :id "some.uuid"
          :authn-instant "1970-01-01T00:00:00.000Z",
          :issue-instant "1970-01-01T00:00:00.000Z",
          :not-before "1970-01-01T00:00:00.000Z",
          :not-on-or-after "1970-01-01T00:10:00.000Z"})

(fact "get-saml-assertion"
      (let [saml-doc (saml/get-saml-assertion saml-props)
            saml (.getDocumentElement saml-doc)]
          (.getNamespaceURI saml) => saml/NS-SAML
          (xp/$x:text "@ID" saml) => (:id saml-props)
          (xp/$x:text "@IssueInstant" saml) => "2011-12-05T17:55:45.199Z"
          (xp/$x:text "//@NotBefore" saml) => "2011-12-05T16:56:07.438Z"
          (xp/$x:text "//@NotOnOrAfter" saml) => "2011-12-05T18:56:07.438Z"
          (xp/$x:text "//@AuthnInstant" saml) => "2011-12-05T17:55:45.199Z"
          (xp/$x:text "//*[local-name()='Subject']//@Format" saml) => "urn:some.id.format"
          (xp/$x:text "*/*[local-name()='NameID']" saml) => "joe"))
