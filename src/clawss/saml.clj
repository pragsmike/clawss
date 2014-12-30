(ns clawss.saml
  (:require [saacl.xml :as xml]
            [saacl.soap :as soap]
            [clj-xpath.core :as xp]
            [clojure.java.io :as io]
            [selmer.parser :as selmer]
            )
  (:import (org.joda.time DateTime)
           (org.joda.time.format ISODateTimeFormat)))

(def NS-SAML "urn:oasis:names:tc:SAML:2.0:assertion")

(defn expand-template [template-name context-map]
  (selmer/render
   (slurp (io/reader (io/resource template-name)))
   context-map))

(defn saml-xml [props]
  (expand-template "saml-template.xml" props))

(defn add-saml-times
  "Given a map, returns a map that has saml timestamp properties
   that, if used in an Assertion, would make it valid for the next ten minutes.
   If given a second DateTime argument, uses that as now; useful for testing."
  ( [props now]   (let [fmt (ISODateTimeFormat/dateTime)
                    later (.plusMinutes now 10)
                    nows (.print fmt now)
                    laters (.print fmt later)]
                (merge props {
                              :authn-instant nows
                              :issue-instant nows
                              :not-before nows
                              :not-on-or-after laters}))
      )
  ( [props] (add-saml-times props (DateTime.)))
  )

(defn next-uuid []
  (str "uuid:" (java.util.UUID/randomUUID)))

(defn get-saml-props
  "Use this when calling add-saml-assertion!.
   Returns a map that has assertion id, subject id, and timestamp properties
   that, if used in an Assertion, would make it valid for the next ten minutes.
   If given assertion-id and DateTime argument, uses those literally instead of generating values."
  ([name-type name]
     (get-saml-props name-type name (next-uuid) (DateTime.))
     )
  ([name-type name assertion-id now]
     (add-saml-times {
                      :nameid name
                      :nameid-format name-type
                      :id assertion-id}
                     now))
  )

(defn get-saml-assertion [saml-props]  (xml/->doc (saml-xml saml-props)))
