(ns clawss.t-it-proxy
  "Integration test using a proxy running in jetty."
  (:require
    [clawss.test-utils :as tu]
    [clawss.handler :as handler]
    [saacl.soap :as soap]
    [saacl.xml :as xml]
    [clj-http.client :as client]
    [environ.core :refer [env]]
    [ring.adapter.jetty :refer [run-jetty]]
    [midje.sweet :refer :all])
  )

; http-client -> clawss-proxy -> https-server

(def payload-proxy-uri "http://localhost:3011/")
(def tester-soap-receiver-uri "http://localhost:3010")

(handler/set-remote-uri-base tester-soap-receiver-uri)

(namespace-state-changes [(before :facts (handler/init))])

(defn check-input-is-soap-post-and-return-soap [request]
  (fact "Input is soap-encapsulated POST"
        (let [soap (soap/->soap (:body request))]
          soap => soap/soap?
          (soap/get-soap-headers soap) => (contains {"request-method" "post"})
          )
        )

  {:status  200
   :headers {"Content-Type" "application/soap+xml"}

   :body    "<?xml version='1.0' encoding='UTF-8'?>
<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' xmlns='urn:clawss'>
  <soap:Body><bort/></soap:Body>
</soap:Envelope>
"
   })



(tu/in-server-with-proxy
  handler/app
  check-input-is-soap-post-and-return-soap

  #_(fact "POST on proxy should POST encapsulated POST to receiver"
        (let [resp (client/post payload-proxy-uri
                                {
                                 :body           "<first/>"
                                 :content-type   :xml
                                 :basic-auth     ["user" "pass"]
                                 :headers        {"X-Api-Version" "2"}
                                 :socket-timeout 1000       ;; in milliseconds
                                 :conn-timeout   1000       ;; in milliseconds
                                 :accept         :xml})]
          (:body resp) => "<?xml version='1.0' encoding='UTF-8'?>\n<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' xmlns='urn:clawss'>\n  <soap:Body><bort/></soap:Body>\n</soap:Envelope>\n"
          )
        )
  )
