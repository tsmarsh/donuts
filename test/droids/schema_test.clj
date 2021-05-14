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
  (-> (lacinia/execute schema query-string nil nil)
      simplify))


(defn create-document [db collection doc]
  (mc/insert-and-return db collection (merge {:_id (ObjectId.)} doc)))

(defn create-documents
  [db data k]
  (doall (map (partial create-document db (name k)) (get data k))))

(defn load-fixture
  [db fname]
  (let [data (-> (io/resource fname)
                 slurp
                 edn/read-string)]
    (doall (map (partial create-documents db data) [:designers :games]))))

(deftest can-find-designers-on-a-game
  (let [server (MongoServer. (MemoryBackend.))
        client (mg/connect (ServerAddress. (.bind server)) (mg/mongo-options {}))
        db (mg/get-db client "test")
        schema (load-schema db)
        loaded (load-fixture db "data.edn")]
    (is (= {:data {:game_by_id {:name "7 Wonders: Duel"}}}
           (q schema "{ game_by_id(id: \"1237\"){ name }}")))
    (is (= {:data {:game_by_id {:name "7 Wonders: Duel", :designers [{:name "Antoine Bauza"} {:name "Bruno Cathala"}]}}}
           (q schema "{ game_by_id(id: \"1237\") { name designers { name }}}")))))

