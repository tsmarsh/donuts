(ns droids.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [outpace.config :refer [defconfig]]
            [monger.core :as mg]
            [droids.schema :as ds])
  (:import (com.mongodb ServerAddress)))

(defconfig ^String server-address "localhost:27017")
(defconfig ^String db-name "test")

(println "Server Address: " server-address "DB_ADDRESS: " (System/getenv "DB_ADDRESS"))

(defn -main []
    (let [client (mg/connect (ServerAddress. server-address) (mg/mongo-options {}))
          db (mg/get-db client db-name)
          schema (ds/load-schema db)
          server (-> schema
                     (lp/service-map {:graphiql true})
                     http/create-server
                     http/start)]
      server))