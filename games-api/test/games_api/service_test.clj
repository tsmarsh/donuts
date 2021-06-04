(ns games-api.service-test
  (:require [clojure.test :refer :all]
            [games-api.test-fixture :as tf]
            [games-api.schema :as s]
            [clj-http.client :as http]
            [games-api.service :as g]
            [monger.core :as mg]
            [schema-generators.generators :as sg]
            [io.pedestal.test :refer :all]
            [io.pedestal.http.route :as route]
            [cheshire.core :as json])

  (:import (com.mongodb ServerAddress)
           (java.net InetSocketAddress)
           (de.bwaldvogel.mongo MongoServer)
           (de.bwaldvogel.mongo.backend.memory MemoryBackend)))


(def backend (MemoryBackend.))

(def db-server (MongoServer. backend))

(def client (mg/connect (ServerAddress. ^InetSocketAddress (.bind db-server)) (mg/mongo-options {})))

(def db (mg/get-db client "test"))

;(def db (atom {}))
;(defn before [f]
;  (reset! db {})
;  (f))

(def routes (g/routes db))

(def service (:io.pedestal.http/service-fn
               (io.pedestal.http/create-servlet (g/service-map routes))))

(def url-for
  "Test url generator."
  (route/url-for-routes routes))

(defn before [f]
  (.dropDatabase backend "test")
  (f))



(use-fixtures :each before)

(deftest read-test
  (is (= "" (:body (response-for service :get (url-for :read :path-params {:id 1414}))))))

(deftest write-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1414})
                :body (json/encode {:name "Tom"})
                :headers {"Content-Type" "application/json"})

  (is (= {"id" "1414" "name" "Tom"}
         (-> (response-for service :get "/document/1414")
             :body
             json/decode))))

(deftest update-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1112})
                :body (json/encode {:name "Archer"})
                :headers {"Content-Type" "application/json"})
  (response-for service
                :post (url-for :update
                               :path-params {:id 1112})
                :body (json/encode {:name "Tilda"})
                :headers {"Content-Type" "application/json"})
  (is (= {"id" "1112" "name" "Tilda"} (-> (response-for service :get (url-for :read :path-params {:id 1112}))
                                          :body
                                          json/decode))))


(deftest delete-test
  (response-for service :put (url-for :create
                                      :path-params {:id 1010}) :body (json/encode {:name "Tom"}))
  (response-for service :delete (url-for :update
                                         :path-params {:id 1010}))
  (is (= "" (:body (response-for service :get (url-for :read :path-params {:id 1112}))))))


