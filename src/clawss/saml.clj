(ns clawss.saml
  (:require [saacl.xml :as xml]
            [clj-xpath.core :as xp]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.java.io :as io]
            [selmer.parser :as selmer]))

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
   If given a second date-time argument, uses that as now; useful for testing."
  ( [props now]   (let [fmt (tf/formatters :date-time-no-ms)
                        later (.plusMinutes now 60)
                        nows (.print fmt now)
                        laters (.print fmt later)]
                    (merge props {
                                  :authn-instant nows
                                  :issue-instant nows
                                  :not-before nows
                                  :not-on-or-after laters}))
      )
  ( [props] (add-saml-times props (t/now))))

(defn next-assertion-id
  "Assertion ID is an xs:ID, per http://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf.
   Must start with a letter or underscore, and can only contain letters, digits, underscores, hyphens, and periods."
  []
  (str (java.util.UUID/randomUUID)))

(defn get-saml-props
  "Use this when calling add-saml-assertion!.
   Returns a map that has assertion id, subject id, and timestamp properties
   that, if used in an Assertion, would make it valid for the next ten minutes.
   If given assertion-id and date-time argument, uses those literally instead of generating values."
  ([name-type name]
     (get-saml-props name-type name (next-assertion-id) (t/now))
     )
  ([name-type name assertion-id now]
     (add-saml-times {
                      :nameid name
                      :nameid-format name-type
                      :id assertion-id}
                     now))
  )

(defn get-saml-assertion [saml-props]  (xml/->doc (saml-xml saml-props)))
