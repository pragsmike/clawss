(ns clawss.t-it-proxy
  "Integration test using a proxy running in jetty."
  (:require
   [clawss.test-utils :as tu]
   [clojure.test :refer :all]
   [clawss.handler :as handler]
   [saacl.soap :as soap]
   [saacl.xml :as xml]
   [clj-http.client :as client]
   [environ.core :refer [env]]
   [ring.adapter.jetty :refer [run-jetty]]))

; http-client -> clawss-proxy -> https-server

(def payload-proxy-uri "http://localhost:3011/")
(def tester-soap-receiver-uri "http://localhost:3010")

(def soap-request (promise))

(handler/set-remote-uri-base tester-soap-receiver-uri)


(defn record-request-and-return-soap [request]

  (deliver soap-request (slurp (:body request)))

  {:status  200
   :headers {"Content-Type" "application/soap+xml"}

   :body    "<?xml version='1.0' encoding='UTF-8'?>
<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns='urn:clawss'>
  <soap:Body><bort/></soap:Body>
</soap:Envelope>
"})

(testing "POST on proxy should POST encapsulated POST to receiver"
  (deftest test-proxy

    (handler/init)

    (tu/in-server-with-proxy
     handler/app
     record-request-and-return-soap

     (let [resp (client/post payload-proxy-uri
                             {
                              :body           "<?xml version='1.0' encoding='UTF-8'?>\n<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns='urn:clawss'>\n  <soap:Body><bort/></soap:Body>\n</soap:Envelope>\n"
                              :content-type   :xml
                              :basic-auth     ["user" "pass"]
                              :headers        {"X-Api-Version" "2"}
                              :socket-timeout 1000 ;; in milliseconds
                              :conn-timeout   1000 ;; in milliseconds
                              :accept         :xml})]

       (is (= (:body resp) "<?xml version='1.0' encoding='UTF-8'?>\n<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns='urn:clawss'>\n  <soap:Body><bort/></soap:Body>\n</soap:Envelope>\n"))
       )
     (let [sr (deref soap-request 5000 "handler didn't deliver request" )
           soap (soap/->soap sr)]
       (is (soap/soap? soap))
       #_(is (contains? (soap/get-soap-headers soap) {"request-method" "post"}))
       )))
  )
