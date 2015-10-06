(defproject clawss "0.1.5"
  :description "clawss: clojure wrapper for web-service security"
  :url "https://bitbucket.org/pragsmike/clawss.git"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [clj-http "2.0.0"]
                 [org.jvnet.staxex/stax-ex "1.7.7"]
                 [com.github.kyleburton/clj-xpath "1.4.5"]
                 [com.sun.xml.wss/xws-security "3.0"
                  :exclusions [ javax.activation/activation
                               javax.xml.stream/stax-api
                               javax.xml.bind/jaxb-api
                               javax.xml.crypto/xmldsig]]
                 [xerces/xercesImpl "2.9.1"]

                 [saacl "0.1.4"]
                 [liberator "0.13"]
                 [compojure "1.4.0"]
                 [selmer "0.9.2" :exclusions [ commons-codec]]
                 [org.clojure/data.zip "0.1.1"]

                 [org.clojure/tools.logging "0.3.1"]
                 [com.taoensso/timbre "4.1.4"]
                 [org.slf4j/slf4j-api "1.7.12"]

                 [log4j
                  "1.2.17"
                  :exclusions
                  [javax.mail/mail
                   javax.jms/jms
                   com.sun.jdmk/jmxtools
                   com.sun.jmx/jmxri]]

                 [ring "1.4.0"
                  :exclusions [joda-time
                               org.clojure/java.classpath]]
                 [ring-server "0.4.0"
                  :exclusions [joda-time]]
                 ]

  :ring {:handler clawss.handler/app,
         :init    clawss.handler/init,
         :destroy clawss.handler/destroy}

  :plugins [[lein-ring "0.8.10" :exclusions [org.clojure/clojure]]
            [lein-environ "1.0.0"]
            ]

  :profiles
  {
   :uberjar {:aot          :all
             :dependencies [[org.slf4j/slf4j-log4j12 "1.7.12"]]
             }
   :production   {:ring    {:open-browser? false,
                            :stacktraces? false,
                            :auto-reload? false}}

   :dev     {:dependencies [[ring-mock "0.1.5"]
                            [ring/ring-devel "1.4.0" :exclusions [org.clojure/java.classpath]]
                            [robert/hooke "1.3.0"]
                            [org.slf4j/slf4j-log4j12 "1.7.12"]
                            ]
             :env          {:dev true }
             :test-paths   ["test-resources"]}})
