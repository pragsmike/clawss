(ns clawss.test-utils
  (:require [midje.sweet :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [saacl.soap :as soap])
  )

(defn start-server
  ([handler]      (start-server handler 3010))
  ([handler port] (run-jetty handler {:port port :join? false}))
  )
(defn stop-server [server] (.stop server))

(defmacro in-server [handler port & body]
  (let [server (gensym)]
    `(let [~server (start-server ~handler ~port)]
       (try
         ~@body
         (finally
           (stop-server ~server)
           ))
       )
    )
  )

(defmacro in-server-with-proxy
  "Utilities for testing HTTP proxy services, such as a clawss proxy.
  You supply a handler function that will receive the backside request (outbound from the proxy).
  You issue a request to the proxy, it will transform it somehow and call the handler you supplied."
  [proxy-handler target-handler & body]

  `(in-server ~proxy-handler 3011
             (in-server ~target-handler 3010 ~@body))
)


(defn build-soap-request [headers body]
  {
   :request-method :post
   :content-type   "application/soap+xml; charset=utf-8"
   :body           (soap/build-soap-xml headers body)
   }
  )
