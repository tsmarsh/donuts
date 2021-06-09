(ns games-api.server
  (:require [games-api.service :as service]
            [io.pedestal.http :as bootstrap]
            [outpace.config :refer [defconfig]]
            [monger.core :as mg])
  (:gen-class)
  (:import (com.mongodb ServerAddress)))

(defonce service-instance nil)

(defconfig ^String server-address "localhost:27017")
(defconfig ^String db-name "test")

(defn create-server []
      (alter-var-root #'service-instance
                      (constantly (bootstrap/create-server
                                    (let [client (mg/connect (ServerAddress. server-address) (mg/mongo-options {}))
                                          db (mg/get-db client db-name)]
                                      (-> (service/service-map (service/routes db :prod))
                                          (assoc ::bootstrap/port (Integer. (or (System/getenv "PORT") "8080")))
                                          (bootstrap/default-interceptors)))))))

(defn start []
      (when-not service-instance
                (create-server))
      (println "Starting server on port" (::bootstrap/port service-instance))
      (bootstrap/start service-instance))

(defn stop []
      (when service-instance
            (bootstrap/stop service-instance)))

(defn -main [& args]
      (start))