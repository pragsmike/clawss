(defproject clawss "0.1.8"
  :description "clawss: clojure wrapper for web-service security"
  :url "https://github.com/pragsmike/clawss.git"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "1.1.0"]
                 [clj-http "3.4.1"]
                 [org.jvnet.staxex/stax-ex "1.7.8"]
                 [com.sun.xml.wss/xws-security "3.0"
                  :exclusions [ javax.activation/activation
                               javax.xml.stream/stax-api
                               javax.xml.bind/jaxb-api
                               javax.xml.crypto/xmldsig]]

                 [saacl "0.1.6"]
                 [liberator "0.14.1"]
                 [compojure "1.5.2"]
                 [selmer "1.10.7" :exclusions [ commons-codec]]
                 [org.clojure/data.zip "0.1.2"]

                 [org.clojure/tools.logging "0.3.1"]
                 [com.taoensso/timbre "4.8.0"]
                 [org.slf4j/slf4j-api "1.7.25"]

                 [log4j
                  "1.2.17"
                  :exclusions
                  [javax.mail/mail
                   javax.jms/jms
                   com.sun.jdmk/jmxtools
                   com.sun.jmx/jmxri]]

                 [ring "1.5.1"
                  :exclusions [joda-time
                               org.clojure/java.classpath]]
                 [ring-server "0.4.0"
                  :exclusions [joda-time]]
                 ]

  :ring {:handler clawss.handler/app,
         :init    clawss.handler/init,
         :destroy clawss.handler/destroy}

  :plugins [[lein-ring "0.10.0" :exclusions [org.clojure/clojure]]
            [lein-environ "1.1.0"]
            ]

  :profiles
  {
   :uberjar {:aot          :all
             :dependencies [[org.slf4j/slf4j-log4j12 "1.7.25"]]
             }
   :production   {:ring    {:open-browser? false,
                            :stacktraces? false,
                            :auto-reload? false}}

   :dev     {:dependencies [[ring-mock "0.1.5"]
                            [ring/ring-devel "1.5.1" :exclusions [org.clojure/java.classpath]]
                            [robert/hooke "1.3.0"]
                            [org.slf4j/slf4j-log4j12 "1.7.25"]
                            ]
             :env          {:dev true }
             :test-paths   ["test-resources"]}})
