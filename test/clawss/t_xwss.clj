(ns clawss.t-xwss
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [clj-xpath.core :as xp]
            [clawss.xwss :as xwss]
            [clawss.saml :as saml]
            [saacl.soap :as soap]
            ))

 (def saml-props {:id "d2"
                 :authn-instant "2011-12-05T17:55:45Z"
                 :issue-instant "2011-12-05T17:55:45Z"
                 :not-before "2011-12-05T16:56:07Z"
                 :not-on-or-after "2011-12-05T18:56:07Z"
                 :nameid-format "urn:some.id.format"
                 :nameid "joe"
                  })


(defn has-header-element? [ doc ns tagname]
  (not (empty? (soap/get-header-elements doc ns tagname))))


(deftest test-xwss-setup
  (is (xwss/processor)))

(deftest test-secure-outbound-message!
      (let [nosig (soap/->soap (io/resource "sample-request-with-headers.xml"))
            withsig (xwss/secure-outbound-message! nosig)
            ]
        (is (has-header-element? withsig xwss/NS-XMLDSIG "Signature"))
        (is (has-header-element? withsig xwss/NS-WSS-UTILITY "Timestamp"))))

(deftest add-message-id!-with-soap-header
  (let [noid (soap/->soap (io/resource "sample-request.xml"))
        withid (xwss/add-message-id! noid)]
    (is (soap/soap? withid))
    (is (has-header-element? withid soap/NS-ADDRESSING "MessageID"))
    (is (re-find #"uuid:.*"
                 (.getTextContent (soap/get-header-element withid soap/NS-ADDRESSING "MessageID"))))))

(deftest test-add-security-with-soap-header
      (let [nosec (soap/->soap (io/resource "sample-request.xml"))
            withsec (xwss/add-security-header! nosec)]
        (is (soap/soap? withsec))
        (is (has-header-element? withsec xwss/NS-WSS-SECEXT "Security"))))

(deftest test-add-saml-assertion!
  (let [nosec (soap/->soap (io/resource "sample-request.xml"))
        withsec (xwss/add-security-header! nosec)
        withsaml (xwss/add-saml-assertion! withsec saml-props)]

    (let [sechdr (soap/get-header-element withsaml xwss/NS-WSS-SECEXT "Security")
          saml (xp/$x:node "//*[local-name() = 'Assertion']" sechdr)]
      (is (= saml/NS-SAML (.getNamespaceURI saml)))
      (is (=  (:id saml-props) (xp/$x:text "@ID" saml)))
      (is (= "2011-12-05T17:55:45Z"  (xp/$x:text "@IssueInstant" saml)))
      (is (= "2011-12-05T16:56:07Z" (xp/$x:text "//@NotBefore" saml)))
      (is (= "2011-12-05T18:56:07Z" (xp/$x:text "//@NotOnOrAfter" saml)))
      (is (= "2011-12-05T17:55:45Z" (xp/$x:text "//@AuthnInstant" saml)))
      (is (= "urn:some.id.format" (xp/$x:text "//*[local-name()='Subject']//@Format" saml)))
      (is (= "joe" (xp/$x:text "*/*[local-name()='NameID']" saml))))))

(deftest test-verify-inbound-message
  (let [nosec (soap/->soap (io/resource "sample-request.xml"))
        withsec (xwss/secure-soap-request! nosec "joe" "some.type")]
    (is (has-header-element? withsec soap/NS-ADDRESSING "MessageID"))
    (is (has-header-element? withsec xwss/NS-WSS-SECEXT "Security"))
    (is (has-header-element? withsec xwss/NS-XMLDSIG "Signature"))
    (is (has-header-element? withsec xwss/NS-WSS-UTILITY "Timestamp"))

    (let [verified (xwss/verify-inbound-message withsec)]
      (is verified))))

(deftest test-verify-inbound-message-bad-1
  (let [withsec (soap/->soap (io/resource "bad-signature-1.xml"))]
    (is (has-header-element? withsec soap/NS-ADDRESSING "MessageID"))
    (is (has-header-element? withsec xwss/NS-WSS-SECEXT "Security"))
    (is (has-header-element? withsec xwss/NS-XMLDSIG "Signature"))
    (is (has-header-element? withsec xwss/NS-WSS-UTILITY "Timestamp"))

    (is (thrown? com.sun.xml.wss.XWSSecurityException (xwss/verify-inbound-message withsec)))))


(deftest test-secure-soap-request!
  (let [nosec (soap/->soap (io/resource "sample-request.xml"))
        withsec (xwss/secure-soap-request! nosec "joe" "some.type")]
    (is (has-header-element? withsec soap/NS-ADDRESSING "MessageID"))
    (is (has-header-element? withsec xwss/NS-WSS-SECEXT "Security"))
    (is (has-header-element? withsec xwss/NS-XMLDSIG "Signature"))
    (is (has-header-element? withsec xwss/NS-WSS-UTILITY "Timestamp"))

    (let [verified (xwss/verify-soap-response! withsec)]
      (is (not (has-header-element? withsec xwss/NS-WSS-SECEXT "Security"))))))


(testing "verify-soap-response! leaves SOAP Fault intact"
  (def soap-fault  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">
<env:Body>
    <env:Fault>
        <faultcode>env:Client</faultcode>
        <faultstring>You did something wrong, but I can't tell you what for security reasons.  It's for your own good.</faultstring>
    </env:Fault>
</env:Body>
</env:Envelope>")

  (deftest test-verify-soap-response!-on-fault
    (let [sf (xwss/verify-soap-response! (soap/->soap soap-fault))]
      (is (= "env:Client" (xp/$x:text "*[local-name()='Fault']/faultcode" (.getSOAPBody sf)))))))
