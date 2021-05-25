(defproject games-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [pedestal-api "0.3.6-20210212.231501-2" :exclusions [prismatic/schema]]
                 [prismatic/schema "1.1.12"]
                 [io.pedestal/pedestal.service "0.5.8"]
                 [io.pedestal/pedestal.jetty "0.5.8"]

                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.30"]
                 [org.slf4j/jcl-over-slf4j "1.7.30"]
                 [org.slf4j/log4j-over-slf4j "1.7.30"]
                 [org.clojure/tools.logging "1.1.0"]]
  :main ^:skip-aot games-api.server
  :uberjar-name "games-api-standalone.jar"
  :profiles {:uberjar {:aot :all}
             :dev     {:repl-options {:init-ns games-api.server}}}
  :repl-options {:init-ns games-api.server})
