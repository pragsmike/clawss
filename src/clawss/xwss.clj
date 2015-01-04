(ns clawss.xwss
  (:import (javax.security.auth.callback CallbackHandler)
           (javax.xml.namespace QName))
  (:require [clojure.java.io :as io]
            [clawss.creds :as creds]
            [clawss.saml :as saml]
            [saacl.soap :as soap]
            [saacl.xml :as xml]))

(def NS-WSS-UTILITY "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd")
(def NS-WSS-SECEXT "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")
(def NS-XMLDSIG  "http://www.w3.org/2000/09/xmldsig#")

(defn create-processor []
  (. (com.sun.xml.wss.XWSSProcessorFactory/newInstance)
     createProcessorForSecurityConfiguration
     (io/input-stream (io/resource "xwss.xml"))
     (reify CallbackHandler
       (handle [this callbacks]
         (let [cb (aget callbacks 0)]
           (when (= (type cb) com.sun.xml.wss.impl.callback.CertificateValidationCallback)
             (.setValidator cb (reify com.sun.xml.wss.impl.callback.CertificateValidationCallback$CertificateValidator
                                 (^boolean validate [this ^java.security.cert.X509Certificate certificate]  true)
                                 )))

           (if (= (type cb) com.sun.xml.wss.impl.callback.SignatureKeyCallback)
             (let [req (.getRequest cb)
                   alias (.getAlias req)
                   keystore (creds/keystore)
                   key-pass (:keystore-pass (creds/get-keystore-registry))]
               (.setPrivateKey req (.getKey keystore alias (char-array key-pass)))
               (.setX509Certificate req (.getCertificate keystore alias)))))))))

(def processor* (delay (create-processor)))

(defn processor [] @processor*)

(defn strip-security-header!
  "Removes the security header from the given SOAPMessage, mutating it in place.
   Returns the mutated message, with the security header removed."
  [message]
  (let [message (soap/->soap message)]
    (.detachNode (soap/get-header-element message clawss.xwss/NS-WSS-SECEXT "Security"))
    message
    )
  )
(defn verify-inbound-message
  [message]
  (let [processor (processor)
        message (soap/->soap message)
        context (. processor createProcessingContext message)]
    #_(. processor verifyInboundMessage context)
    (strip-security-header! message)
    )
  )

(defn add-xml-signature!
  "Accepts an XML SOAP message in any format accepted by saacl.soap/->soap.
  Returns a javax.xml.soap.SOAPMessage that has the same content but with a signature added.
  NOTE: If the input is already a SOAPMessage, it will be modified in place."
  [message]
  (let [processor (processor)
        context (. processor createProcessingContext (soap/->soap message))]
    (. processor secureOutboundMessage context)))

(defn next-uuid []
  (str "uuid:" (java.util.UUID/randomUUID)))

(defn add-message-id!
  "Accepts an XML document, either a string or a Document.
  Returns that same document, mutated, that holds a MessageID header."
  [message]
  (let [header (.getSOAPHeader message)
        element (.addHeaderElement header (QName. soap/NS-ADDRESSING "MessageID"))]
    (.setTextContent element (next-uuid))
    message
    ))


(defn add-security-header!
  "Accepts a SOAPMessage.
  Returns that same document, mutated, that holds a Security header."
  [message]
  (let [header (.getSOAPHeader message)]
    (.addHeaderElement header (QName. NS-WSS-SECEXT "Security"))
    message))

(defn add-saml-assertion!
  "Accepts a SOAPMessage.  Returns that same message, with a SAML
   Assertion added to the Security header."
  [message saml-props]
  (let [sechdr (soap/get-header-element message NS-WSS-SECEXT "Security")
        saml-el (.getDocumentElement (saml/get-saml-assertion saml-props))
        saml-soap-el (.createElement (javax.xml.soap.SOAPFactory/newInstance) saml-el)]
    (.addChildElement sechdr saml-soap-el)
    )
  message
  )

(defn secure-message
  [message subject-name subject-name-type]
  (-> message
      (soap/->soap)
      (add-message-id!)
      (add-security-header!)
      (add-saml-assertion! (saml/get-saml-props subject-name-type subject-name))
      (add-xml-signature!))
  )


(defn wrap-xmlsig
  "Client (clj-http) middleware to add XML signature."
  [client]
  (fn [req]
    (client (assoc req :body (secure-message
                              (:body req)
                              (or (:subject-name req) "")
                              (or (:subject-name-type req) "")
                              )))))
