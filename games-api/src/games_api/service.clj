(ns games-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as bp]
            [games-api.repository :as r]
            [outpace.config :refer [defconfig]]
            [pedestal-api.routes :as api]
            [pedestal-api.core :as capi]
            [schema.core :as s]
            [route-swagger.doc :as sw.doc]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))
(def no-content (partial response 204))
(def not-found (partial response 404))

(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [request (:request context)
                 response (ok context)]
             (assoc context :response response)))})


(defn reader [db schema]
  (capi/annotate
    {:summary     "Get by id"
     :parameters  {:path {:id s/Int}}
     :responses   {200 {:body schema}
                   404 {:body s/Str}}
     :operationId :read}
    (interceptor
      {
       :name  ::doc-reader
       :enter (fn [ctx]
                (let [id (get-in ctx [:request :path-params :id])]
                  (if-let [n (r/readDoc db id)]
                    (assoc ctx :response (ok n))
                    (assoc ctx :response (not-found "Document not found")))))})))


(defn writer [db schema]
  (capi/annotate
    {:summary     "Create doc at path"
     :parameters  {:body schema}
     :responses   {201 {:body {:id s/Uuid}}}
     :operationId :create}
    (interceptor
      {
       :name  ::doc-writer
       :enter (fn [{req :request :as ctx}]
                (let [body (get req :json-params)
                      id (get-in req [:path-params :id])
                      res (:response ctx)]
                  (r/writeDoc db id body)
                  (assoc ctx :response (merge res (created body)))))})))


(defn updater [db schema]
  (capi/annotate {:summary     "Update"
                  :parameters  {:path {:id s/Uuid}
                                :body schema}
                  :responses   {202 {:body s/Str}}
                  :operationId :update-pet}

                 (interceptor
                   {
                    :name  ::doc-updater
                    :enter (fn [{req :request :as ctx}]
                             (let [body (get req :json-params)
                                   id (get-in req [:path-params :id])
                                   res (:response ctx)]
                               (r/changeDoc db id body)
                               (assoc ctx :response (merge res (accepted body)))))})))


(defn deleter [db]
  (capi/annotate {:summary     "Remove Document"
                  :parameters  {:path {:id s/Uuid}}
                  :responses   {200 {:body s/Str}}
                  :operationId :remove}
                 (interceptor
                   {
                    :name  ::doc-reader
                    :enter (fn [ctx]
                             (let [id (get-in ctx [:request :path-params :id])]
                               (r/deleteDoc db id)
                               (assoc ctx :response (no-content "DELETED"))))})))



(def api-name "document")

(defn routes [db schema]
  (s/with-fn-validation
    (let [doc {:info {:title       "Simple Donut"
                      :description "Ask Tom, he likes donuts"
                      :version     "0.0.0"}
               :tags [{:name         "Donuts"
                       :description  "Everything about your Donuts"
                       :externalDocs {:description "Find out more"
                                      :url         "http://swagger.io"}}]}
          rs #{
               [(format "/%s/:id" api-name) :put [(capi/body-params) (writer db schema)] :route-name :create]
               [(format "/%s/:id" api-name) :get [(capi/negotiate-response) #_(capi/validate-response) (reader db schema)] :route-name :read]
               [(format "/%s/:id" api-name) :post [(capi/body-params) (updater db schema)] :route-name :update]
               [(format "/%s/:id" api-name) :delete (deleter db) :route-name :delete]
               }]
      (-> rs
          route/expand-routes
          (api/update-handler-swagger (api/comp->> api/default-operation-ids
                                                   api/default-empty-parameters))
          (sw.doc/with-swagger (merge {:basePath ""} doc))))
  )
)


(defn service-map [rtes]
  {::http/routes rtes
   ::http/type   :jetty
   ::http/port   8890})

(defn start []
  (http/start (http/create-server service-map)))

;; For interactive development
(defonce server (atom nil))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                        (assoc service-map
                          ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))


