(ns games-api.repository
  (:import (clojure.lang   Atom)))

(defprotocol DocumentRepository
  (writeDoc [db id body])
  (readDoc [db id])
  (changeDoc [db id body])
  (deleteDoc [db id]))



