(ns games-api.service-poc
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.interceptor.chain :refer [terminate]]
            [io.pedestal.interceptor :refer [interceptor]]
            [pedestal-api.core :as api]
            [schema.core :as s]
            [games-api.schema :as gs]
            [monger.collection :as mc]
            [outpace.config :refer [defconfig]]))

(defconfig ^String doc-name "document")


(defn create-doc
  [db]
  (api/annotate
    {:summary     "Create a pet"
     :parameters  {:body gs/Document}
     :responses   {201 {:body {:id s/Uuid}}}
     :operationId :create-pet}
    (interceptor {:name  ::create
                  :enter (fn [{:keys [request] :as ctx}]
                           (let [body (:body-params request)]
                             (mc/create db doc-name body)
                             (assoc ctx :response {:status 201
                                                   :body   body})))})))


;(defn load-by-id [db]
;  (api/annotate
;    {:summary    "Load a doc by id"
;     :parameters {:path {:id s/Uuid}}
;     :responses  {404 {:body s/Str}}}
;    (interceptor
;      {:name  ::load-by-id
;       :enter (fn
;                [{:keys [request] :as context}]
;
;                (if-let [doc (first (mc/find-maps db doc-name {:id (get-in request [:path-params :id])}))]
;                  (update context :request assoc :document doc)
;                  (-> context terminate (assoc :response {:status 404
;                                                          :body   "No document found with this id"}))))})))

;(def read-doc
;  (api/annotate {:summary     "Get by id"
;                 :parameters  {:path {:id s/Uuid}}
;                 :responses   {200 {:body gs/Document}
;                               404 {:body s/Str}}
;                 :operationId :read}
;
;                (interceptor {:name  ::read
;                              :enter (fn [{:keys [document] :as ctx}]
;                                       (assoc ctx :response
;                                                  {:status 200
;                                                   :body   document}))})))
;
;(defn change-doc [db]
;  (api/annotate {:summary     "Update"
;                 :parameters  {:path {:id s/Uuid}
;                               :body gs/Document}
;                 :responses   {200 {:body s/Str}}
;                 :operationId :update-pet}
;
;                (interceptor {:name  ::change
;                              :enter (fn [{:keys [request document]} as ctx]
;                                       (let [updated-doc (merge document (:body-params request))]
;                                         (mc/update-by-id db
;                                                          doc-name
;                                                          (get-in request [:path-params :id])
;                                                          updated-doc)
;                                         (assoc ctx :response
;                                                    {:status 200
;                                                     :body   updated-doc})))})))
;
;(defn remove-doc
;  [db]
;  (api/annotate
;    {:summary     "Remove Document"
;     :parameters  {:path {:id s/Uuid}}
;     :responses   {200 {:body s/Str}}
;     :operationId :remove}
;    (interceptor
;      {:name  ::rm
;       :enter (fn [ctx]
;                (let [document (get-in ctx [:request :document])]
;                  (mc/remove-by-id db doc-name (:id document))
;                  (assoc ctx :response
;                             {:status 204})))})))

(def ^:dynamic db {})

(s/with-fn-validation
  (api/defroutes routes
                 {:info {:title       "Simple Donut"
                         :description "Ask Tom, he likes donuts"
                         :version     "0.0.0"}
                  :tags [{:name         "Donuts"
                          :description  "Everything about your Donuts"
                          :externalDocs {:description "Find out more"
                                         :url         "http://swagger.io"}}]}
                 [[["/" ^:interceptors [api/error-responses
                                        (api/negotiate-response)
                                        (api/body-params)
                                        api/common-body
                                        (api/coerce-request)
                                        (api/validate-response)]
                    ["/api" ^:interceptors [(api/doc {:tags ["api"]})]
                     ["/" {:put (create-doc db)}]
                     #_["/:id" ^:interceptors [(load-by-id db)]
                        {:get    read-doc
                         :post   (change-doc db)
                         :delete (remove-doc db)}]]

                    ["/swagger.json" {:get api/swagger-json}]
                    ["/*resource" {:get api/swagger-ui}]]]]))

(defn service
  [newdb env]
  (binding [db newdb]
    {:env                      env
     ::bootstrap/routes        #(deref #'routes)
     ;; linear-search, and declaring the swagger-ui handler last in the routes,
     ;; is important to avoid the splat param for the UI matching API routes
     ::bootstrap/router        :linear-search
     ::bootstrap/resource-path "/public"
     ::bootstrap/type          :jetty
     ::bootstrap/port          8080
     ::bootstrap/join?         false}))
