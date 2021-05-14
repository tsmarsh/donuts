(ns droids.schema
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [clojure.edn :as edn]
    [monger.collection :as mc]))



(defn resolve-game-by-id
  [db context args value]
  (let [{:keys [id]} args]
    (first (mc/find-maps db "games" {:id id}))))

(defn resolve-board-game-designers
  [db context args board-game]
  (map (fn [designer] (first (mc/find-maps db "designers" {:id designer}))) (:designers board-game)))

(defn resolve-designer-games
  [db context args designer]
  (let [{:keys [id]} designer]
    (mc/find-maps db "games" {:designers {"$elemMatch" {"$eq" id}}})))



(defn resolver-map
  [db]
    {:query/game-by-id (partial resolve-game-by-id db)
     :BoardGame/designers (partial resolve-board-game-designers db)
     :Designer/games (partial resolve-designer-games db)})

(defn load-schema
  [db]
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map db))
      schema/compile))