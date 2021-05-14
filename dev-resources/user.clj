(ns user
  (:require
    [droids.schema :as s]
    [com.walmartlabs.lacinia :as lacinia]
    [clojure.walk :as walk]
    [com.walmartlabs.lacinia.pedestal :as lp]
    [io.pedestal.http :as http]
    [clojure.java.browse :refer [browse-url]]))

(defonce server nil)

#_(defn start-server
  [_]
  (let [server (-> schema
                   (lp/service-map {:graphiql true})
                   http/create-server
                   http/start)]
    (browse-url "http://localhost:8888/")
    server))

#_(defn stop-server
  [server]
  (http/stop server)
  nil)

#_(defn start
  []
  (alter-var-root #'server start-server)
  :started)

#_(defn stop
  []
  (alter-var-root #'server stop-server)
  :stopped)
