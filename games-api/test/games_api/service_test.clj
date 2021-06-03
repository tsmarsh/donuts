(ns games-api.service-test
  (:require [clojure.test :refer :all]
            [games-api.test-fixture :as tf]
            [games-api.schema :as s]
            [clj-http.client :as http]
            [games-api.service :as g]
            [monger.core :as mg]
            [schema-generators.generators :as sg]
            [io.pedestal.test :refer :all]
            [io.pedestal.http.route :as route])

  (:import (com.mongodb ServerAddress)
           (java.net InetSocketAddress)
           (de.bwaldvogel.mongo MongoServer)
           (de.bwaldvogel.mongo.backend.memory MemoryBackend)))

#_(def url-for (partial tf/url-for (deref routes)))

;(def db-server (MongoServer. (MemoryBackend.)))
;
;(def client (mg/connect (ServerAddress. ^InetSocketAddress (.bind db-server)) (mg/mongo-options {})))
;
;(def db (mg/get-db client "test"))

(def service (:io.pedestal.http/service-fn
               (io.pedestal.http/create-servlet g/service-map)))

(def url-for
  "Test url generator."
  (route/url-for-routes g/routes))



(deftest read-test
  (is (= "Hello, world!" (:body (response-for service :get (url-for :read
                                                                    :path-params {:id 1414}))))))

(deftest write-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1414}) :body "Tom")
  (is (= "Hello, Tom!" (:body (response-for service :get "/document/1414")))))

(deftest update-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1112}) :body "Archer")
  (response-for service :post (url-for :update
                                      :path-params {:id 1112}) :body "Tilda")
  (is (= "Hello, Tilda!" (:body (response-for service :get (url-for :read :path-params {:id 1112}))))))

(deftest update-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1010}) :body "Archer")
  (response-for service :delete (url-for :update
                                       :path-params {:id 1010}))
  (is (= "Hello, world!" (:body (response-for service :get (url-for :read :path-params {:id 1112}))))))


