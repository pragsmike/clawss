(ns clawss.t-xwss
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [clj-xpath.core :as xp]
            [clawss.xwss :as xwss]
            [clawss.saml :as saml]
            [saacl.soap :as soap]
            [clawss.t-saml :as t-saml]
            ))

(defn has-header-element? [ doc ns tagname]
  (not (empty? (soap/get-header-elements doc ns tagname))))

(facts "add-signature!"
       (fact "Setup"
             (xwss/processor) => truthy)

       (fact "add-xml-signature!"
             (let [nosig (soap/->soap (io/resource "sample-request-with-headers.xml"))
                   withsig (xwss/add-xml-signature! nosig)
                   ]
               (has-header-element? withsig xwss/NS-XMLDSIG "Signature") => truthy
               (has-header-element? withsig xwss/NS-WSS-UTILITY "Timestamp") => truthy)))

(facts "add-message-id!"
       (fact "add-message-id! with existing soap Header element"
             (let [noid (soap/->soap (io/resource "sample-request.xml"))
                   withid (xwss/add-message-id! noid)]
               withid => soap/soap?
               (has-header-element? withid soap/NS-ADDRESSING "MessageID") => truthy
               (.getTextContent (soap/get-header-element withid soap/NS-ADDRESSING "MessageID")) => #"uuid:.*"

               )
             )
       (fact "add-message-id! on message with no existing Header"))

(facts "add-security-header!"
       (fact "add-security-header! with existing soap Header element"
             (let [nosec (soap/->soap (io/resource "sample-request.xml"))
                   withsec (xwss/add-security-header! nosec)]
               withsec => soap/soap?
               (has-header-element? withsec xwss/NS-WSS-SECEXT "Security") => truthy
               )
             ))

(fact "add-saml-assertion!"
      (let [nosec (soap/->soap (io/resource "sample-request.xml"))
            withsec (xwss/add-security-header! nosec)
            withsaml (xwss/add-saml-assertion! withsec t-saml/saml-props)]

        (let [sechdr (soap/get-header-element withsaml xwss/NS-WSS-SECEXT "Security")
              saml (xp/$x:node "//*[local-name() = 'Assertion']" sechdr)]
          (.getNamespaceURI saml) => saml/NS-SAML
          (xp/$x:text "@ID" saml) => (:id t-saml/saml-props)
          (xp/$x:text "@IssueInstant" saml) => "2011-12-05T17:55:45.199Z"
          (xp/$x:text "//@NotBefore" saml) => "2011-12-05T16:56:07.438Z"
          (xp/$x:text "//@NotOnOrAfter" saml) => "2011-12-05T18:56:07.438Z"
          (xp/$x:text "//@AuthnInstant" saml) => "2011-12-05T17:55:45.199Z"
          (xp/$x:text "//*[local-name()='Subject']//@Format" saml) => "urn:some.id.format"
          (xp/$x:text "*/*[local-name()='NameID']" saml) => "joe")))

(facts "secure-message"
       (let [nosec (soap/->soap (io/resource "sample-request.xml"))
             withsec (xwss/secure-message nosec)]
         (has-header-element? withsec soap/NS-ADDRESSING "MessageID") => truthy
         (has-header-element? withsec xwss/NS-WSS-SECEXT "Security") => truthy
         (has-header-element? withsec xwss/NS-XMLDSIG "Signature") => truthy
         (has-header-element? withsec xwss/NS-WSS-UTILITY "Timestamp") => truthy))
