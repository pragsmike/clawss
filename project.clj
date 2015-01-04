(defproject clawss "0.1.1-SNAPSHOT"
  :description "clawss: clojure wrapper for web-service security"
  :url "https://bitbucket.org/pragsmike/clawss.git"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [clj-http "1.0.1"]

                 [com.github.kyleburton/clj-xpath "1.4.4"]
                 [com.sun.xml.wss/xws-security "3.0"
                  :exclusions [ javax.activation/activation
                               javax.xml.stream/stax-api
                               javax.xml.bind/jaxb-api
                               javax.xml.crypto/xmldsig]]
                 [xerces/xercesImpl "2.9.1"]

                 [saacl "0.1.3-SNAPSHOT"]
                 [liberator "0.12.0"]
                 [compojure "1.1.8"]
                 [selmer "0.7.8" :exclusions [ commons-codec]]
                 [org.clojure/data.zip "0.1.1"]

                 [org.clojure/tools.logging "0.3.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [org.slf4j/slf4j-api "1.7.5"]

                 [log4j
                  "1.2.17"
                  :exclusions
                  [javax.mail/mail
                   javax.jms/jms
                   com.sun.jdmk/jmxtools
                   com.sun.jmx/jmxri]]

                 [ring "1.3.1"
                  :exclusions [joda-time
                               org.clojure/java.classpath]]
                 [ring-server "0.3.1"
                  :exclusions [joda-time]]

                 [tailrecursion/ring-proxy "2.0.0-SNAPSHOT"]
                 ]

  :ring {:handler clawss.handler/app,
         :init    clawss.handler/init,
         :destroy clawss.handler/destroy}

  :plugins [[lein-nevam "0.1.2" :exclusions [org.clojure/clojure]]
            [lein-ring "0.8.10" :exclusions [org.clojure/clojure]]
            ;[lein-gorilla "0.2.0"]
            [lein-environ "1.0.0"]
            ]

  :profiles
  {
   :uberjar {:aot          :all
             :dependencies [[org.slf4j/slf4j-log4j12 "1.7.5"]]
             }
   :production   {:ring    {:open-browser? false,
                            :stacktraces? false,
                            :auto-reload? false}}

   :dev     {:dependencies [[ring-mock "0.1.5"]
                            [ring/ring-devel "1.3.1" :exclusions [org.clojure/java.classpath]]
                            [midje "1.6.3"]
                            [robert/hooke "1.3.0"]
                            [org.slf4j/slf4j-log4j12 "1.7.5"]
                            ]
             :env          {:dev true
                            }
             :test-paths   ["test-resources"]
                                        ;:hooks [leiningen.plantuml]
             :plugins      [[lein-midje "3.1.3"]
                            [cider/cider-nrepl "0.7.0"]
                            [lein-marginalia "0.7.1"]
                            [lein-plantuml "0.1.3"]]
             :repl-options {:init (use 'midje.repl)}
             :plantuml     [["doc/*.puml" :png "target"]
                            ["presentation/*.txt" :svg]]}})
