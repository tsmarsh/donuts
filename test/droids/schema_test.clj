(ns droids.schema-test
  (:require [clojure.test :refer :all])
  (:require [droids.schema :refer [load-schema]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [com.walmartlabs.lacinia :as lacinia])
  (:import (de.bwaldvogel.mongo MongoServer)
           (de.bwaldvogel.mongo.backend.memory MemoryBackend)
           (org.bson.types ObjectId)
           (com.mongodb ServerAddress)
           (clojure.lang IPersistentMap)))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
    (fn [node]
      (cond
        (instance? IPersistentMap node)
        (into {} node)

        (seq? node)
        (vec node)

        :else
        node))
    m))


(defn q
  [schema query-string]
  (simplify (lacinia/execute schema query-string nil nil)))

(defn create-documents
  [db data k]
  (let [collection (name k)
        docs (get data k)]
    (doall
      (map #(mc/insert-and-return db
                                  collection
                                  (merge {:_id (ObjectId.)} %))
           docs))))

(defn load-fixture
  [db fname]
  (let [data (-> (io/resource fname)
                 slurp
                 edn/read-string)]
    (doall (map (partial create-documents db data) [:designers :games]))))

(deftest loads-the-schema
  (let [server (MongoServer. (MemoryBackend.))
        client (mg/connect (ServerAddress. (.bind server)) (mg/mongo-options {}))
        db (mg/get-db client "test")
        schema (load-schema db)
        _ (load-fixture db "data.edn")]
    (testing "Can pull back a game by id"
      (is (= {:data {:game_by_id {:name "7 Wonders: Duel" :max_players 2}}}
             (q schema "{ game_by_id(id: \"1237\"){ name max_players}}"))))
    (testing "Can integrate with designer data"
      (is (= {:data {:game_by_id {:name "7 Wonders: Duel", :designers [{:name "Antoine Bauza"} {:name "Bruno Cathala"}]}}}
             (q schema "{ game_by_id(id: \"1237\") { name designers { name }}}"))))
    (testing "Can integrate with a designers games"
      (is (= {:data {:game_by_id {:designers [{:games [{:name "Tiny Epic Galaxies"}]}]}}}
             (q schema "{ game_by_id(id: \"1236\") {designers { games { name }}}}"))))))

