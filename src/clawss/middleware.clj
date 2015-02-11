(ns clawss.middleware
  (:require [saacl.xml :as xml]
            [saacl.soap :as soap]
            [clawss.xwss :as xwss]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [clj-stacktrace.repl :as strp]
            ))


(defn wrap-verify-response
  "Client (clj-http) middleware to verify and strip security headers.
   If the response code is not 200, returns the response unchanged.
   If an error occurs, returns the response unchanged except that the
   key :soap-exception will be set to the exception that occurred."
  [client]
  (fn [req]
    (let [resp (client req)]
      (if (= 200 (:status resp))
        (try (assoc resp :body
                    (xwss/verify-soap-response!
                     (:body resp)))
             (catch Exception e
               (assoc resp :soap-exception e) ))
        resp))))

(defn wrap-secure-request
  "Client (clj-http) middleware to add XML signature."
  [client]
  (fn [req]
    (client (assoc req :body (xwss/secure-soap-request!
                              (:body req)
                              (or (:subject-name req) "")
                              (or (:subject-name-type req) ""))))))


(defn wrap-request-body-to-string
  "Transform request body from various kinds of XML (Document, SOAPMessage) into string."
  [client]
  (fn [req]
    (client
      (if-let [bod (:body req)]
        (assoc req :body (xml/->string bod))
        req
        )
      )
    )
  )
(defn wrap-response-body-to-string
  "Transform response body from various kinds of XML (Document, SOAPMessage) into string."
  [client]
  (fn [req]
    (let [resp (client req)]
      (assoc resp :body (saacl.xml/->string (:body resp))))))

(defn wrap-exception-logging [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (log/debug "Exception:\n%s" (strp/pst-str e))
        (throw e)))))

(defn log-request [handler]
  (if (env :dev)
    (fn [req]
      (log/debug req)
      (handler req))
    handler))

(ns ring.proxy)
(defn wrap-cookies [client]
  (fn [req]
    (client req)))
