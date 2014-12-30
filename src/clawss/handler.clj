(ns clawss.handler
  "Ring handler for the clawss proxy service."
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]

            [clawss.ring-proxy :as rp]
            [clawss.middleware :as middleware]
            [clawss.creds :as creds]
            [clj-http.client :as client]

            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [api]]
            [liberator.core :refer [defresource resource request-method-in]]
            [saacl.xml :as xml]))

(timbre/refer-timbre)

(def remote-uri-base (atom (env :remote-uri-base)))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
   [:appenders :rotor]
   {:min-level             :debug,
    :enabled?              true,
    :async?                false,
    :max-message-per-msecs nil,
    :fn                    rotor/appender-fn})
  (timbre/set-config!
   [:shared-appender-config :rotor]
   {:path "clawss.log", :max-size (* 512 1024), :backlog 10})

  (info "clawss proxy started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (info "clawss proxy is shutting down..."))


(def http-opts (creds/get-keystore-registry) )

(defn set-remote-uri-base [uri] (reset! remote-uri-base uri))
(defn get-remote-uri-base [req] @remote-uri-base)

(defroutes api-routes
           (ANY "/" request (resource
                              :handle-ok "clawss proxy"
                              :etag "fixed-etag"
                              :available-media-types ["text/plain"]))

            (route/not-found "Not Found")
           )


(defn add-headers [req header-map]
  (assoc req :headers (merge (get req :headers {}) header-map))
  )

(defn wrap-add-auth-headers
  "Useful during testing."
  [client]
  (fn [req]
    (client (-> req
                (merge {:basic-auth ["myid" ""]})
                (add-headers { "some-other" "value"})))))

(def client-middleware (concat client/default-middleware
                               [
                                wrap-add-auth-headers
                                middleware/wrap-xml-to-string
                                ]))

(defn with-client-middleware
  "Tells the proxy's http client to use a particular chain of middleware functions."
  [client client-middleware]
  (fn [req]
    (client/with-middleware client-middleware
      (client req)
      ))
    )

(def app
  (-> api-routes
      (rp/wrap-proxy get-remote-uri-base http-opts)
      (with-client-middleware client-middleware)
      (middleware/wrap-exception-logging)
      (middleware/log-request)
;      (wrap-stacktrace)
      ))
