(ns games-api.repository-test
  (:require [clojure.test :refer :all])
  (:require [games-api.repository :refer :all]
            [monger.core :as mg])
  (:import (de.bwaldvogel.mongo.backend.memory MemoryBackend)
           (de.bwaldvogel.mongo MongoServer)
           (com.mongodb ServerAddress)
           (java.net InetSocketAddress)))

(deftest atom-db
  (testing "can write to an atom"
    (let [db (atom {})]
      (writeDoc db 1414 {:name "foo"})
      (is (= "foo" (:name (readDoc db 1414))))
      (is (nil? (readDoc db 1010)))
      (changeDoc db 1414 {:name "bar"})
      (is (= "bar" (:name (readDoc db 1414))))
      (deleteDoc db 1414)
      (is (nil? (readDoc db 1414))))))


(deftest mongo-db
  (testing "can write to an atom"
    (let [backend (MemoryBackend.)
          db-server (MongoServer. backend)
          client (mg/connect (ServerAddress. ^InetSocketAddress (.bind db-server)) (mg/mongo-options {}))
          db (mg/get-db client "test")]
      (writeDoc db 1414 {:name "foo"})
      (is (= "foo" (:name (readDoc db 1414))))
      (is (nil? (readDoc db 1010)))
      (changeDoc db 1414 {:name "bar"})
      (is (= "bar" (:name (readDoc db 1414))))
      (deleteDoc db 1414)
      (is (nil? (readDoc db 1414))))))

