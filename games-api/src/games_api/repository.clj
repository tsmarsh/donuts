(ns games-api.repository
  (:require [monger.collection :as mc]
            [outpace.config :refer [defconfig]])
  (:import (clojure.lang Atom)
           (com.mongodb DB)
           (org.bson.types ObjectId)))

(defprotocol DocumentRepository
  (writeDoc [db id body])
  (readDoc [db id])
  (changeDoc [db id body])
  (deleteDoc [db id]))

(defconfig ^String doc-name "document")

(extend Atom
  DocumentRepository
  {
   :writeDoc  (fn [db id body]
                (swap! db assoc id body))
   :readDoc   (fn [db id]
                (get @db id "world"))
   :changeDoc (fn [db id body]
                (swap! db assoc id body))
   :deleteDoc (fn [db id]
                (swap! db dissoc id))
   })

(extend DB
  DocumentRepository
  {
   :writeDoc  (fn [db id body]
                (let [oid (ObjectId.)]
                  (mc/insert-and-return db doc-name (assoc body :id id :_id oid))))
   :readDoc   (fn [db id]
                (dissoc (first (mc/find-maps db doc-name {:id id})) :_id))
   :changeDoc (fn [db id body]
                (mc/update db doc-name {:id id} body))
   :deleteDoc (fn [db id]
                (mc/remove db doc-name {:id id}))
   })

