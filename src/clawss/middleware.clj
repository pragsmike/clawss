(ns clawss.middleware
  (:require [saacl.xml :as xml]
            [saacl.soap :as soap]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [clj-stacktrace.repl :as strp]
            ))


(defn wrap-xml-to-string
  "Transform request body from various kinds of XML (Document, SOAPMessage) into string."
  [client]
  (fn [req]
    ; TODO convert response body also?
    (client
     (assoc req :body (xml/->string (:body req))))))


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
