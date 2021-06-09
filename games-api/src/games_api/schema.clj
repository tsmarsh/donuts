(ns games-api.schema
  (:require [schema.core :as s]))



(s/defschema Game {
                       :id                           s/Uuid
                       :name                         s/Str
                       (s/optional-key :summary)     s/Str
                       (s/optional-key :description) s/Str
                       (s/optional-key :min_players) s/Int
                       (s/optional-key :max_players) s/Int
                       (s/optional-key :play_time)   s/Int})


(s/defschema Designer {:id                     s/Uuid
                       :name                   s/Str
                       (s/optional-key :url)   s/Str
                       (s/optional-key :games) [Game]})
