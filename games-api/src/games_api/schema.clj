(ns games-api.schema
  (:require [schema.core :as s]))



(s/defschema Document {
                       :id                           s/Uuid
                       :name                         s/Str
                       (s/optional-key :summary)     s/Str
                       (s/optional-key :description) s/Str
                       (s/optional-key :min_players) s/Int
                       (s/optional-key :max_players) s/Int
                       (s/optional-key :play_time)   s/Int})


;(s/defschema Designer {:id                     s/Uuid
;                       :name                   s/Str
;                       (s/optional-key :url)   s/Str
;                       (s/optional-key :games) [Game]})

;(s/defschema Pet
;             {:name s/Str
;              :type s/Str
;              :age  s/Int})
;
;(s/defschema PetWithId
;             (assoc Pet (s/optional-key :id) s/Uuid))
