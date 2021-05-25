(ns user
  (:require
    [droids.schema :as s]
    [com.walmartlabs.lacinia :as lacinia]
    [clojure.walk :as walk]
    [com.walmartlabs.lacinia.pedestal :as lp]
    [io.pedestal.http :as http]
    [clojure.java.browse :refer [browse-url]]
    [monger.core :as mg])
  (:import (de.bwaldvogel.mongo MongoServer)
           (de.bwaldvogel.mongo.backend.memory MemoryBackend)
           (com.mongodb ServerAddress)))

(defonce server nil)

(defn start-server
  [_]
  (let [db-server (MongoServer. (MemoryBackend.))
        client (mg/connect (ServerAddress. (.bind server)) (mg/mongo-options {}))
        db (mg/get-db client "test")
        schema (s/load-schema db)
        server (-> schema
                   (lp/service-map {:graphiql true})
                   http/create-server
                   http/start)]
    (browse-url "http://localhost:8888/")
    server))

(defn stop-server
  [server]
  (http/stop server)
  nil)

(defn start
  []
  (alter-var-root #'server start-server)
  :started)

(defn stop
  []
  (alter-var-root #'server stop-server)
  :stopped)
