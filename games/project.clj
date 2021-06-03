(defproject tsmarsh/games "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.walmartlabs/lacinia "0.38.0"]
                 [com.walmartlabs/lacinia-pedestal "0.15.0"]
                 [io.aviso/logging "0.3.2"]
                 [com.novemberain/monger "3.5.0"]
                 [com.outpace/config "0.13.5"]]

  :profiles {:dev {:dependencies [[de.bwaldvogel/mongo-java-server "1.38.0"]]}}
  :aliases {"config" ["run" "-m" "outpace.config.generate"]}
  :main droids.core
  :repl-options {:init-ns user})
