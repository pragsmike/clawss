(ns clawss.ring-proxy
  (:require [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.adapter.jetty      :refer [run-jetty]]
            [clojure.string          :refer [join split]]
            [clj-http.client         :refer [request]]))

(defn prepare-cookies
  "Removes the :domain and :secure keys and converts the :expires key (a Date)
  to a string in the ring response map resp. Returns resp with cookies properly
  munged."
  [resp]
  (let [prepare #(-> (update-in % [1 :expires] str)
                     (update-in [1] dissoc :domain :secure))]
    (assoc resp :cookies (into {} (map prepare (:cookies resp))))))

(defn slurp-binary
  "Reads len bytes from InputStream is and returns a byte array."
  [^java.io.InputStream is len]
  (with-open [rdr is]
    (let [buf (byte-array len)]
      (.read rdr buf)
      buf)))

(defn wrap-proxy
  "Proxies the given ring request to the URI computed by get-remote-uri,
   a function that takes a ring request as argument."
  [handler get-remote-uri & [http-opts]]
  (fn [req]
    (if-let [remote-uri (get-remote-uri req)]
      (-> (merge {:method (:request-method req)
                  :url remote-uri
                  :orig-uri (:uri req)
                  :headers (dissoc (:headers req) "host" "content-length")
                  :body (if-let [len (get-in req [:headers "content-length"])]
                          (slurp-binary (:body req) (Integer/parseInt len)))
                  :follow-redirects true
                  :throw-exceptions false
                  :as :stream} http-opts)
          request
          prepare-cookies)
      (handler req))
    ))
