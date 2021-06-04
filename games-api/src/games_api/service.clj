(ns games-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as bp]
            [games-api.repository :as r]
            [outpace.config :refer [defconfig]]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))
(def no-content (partial response 204))

(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [request (:request context)
                 response (ok context)]
             (assoc context :response response)))})


(defn reader [db]
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])
                    n (r/readDoc db id)]
                (assoc ctx :response (ok n))))}))


(defn writer [db]
  (interceptor
    {
     :name  :doc-writer
     :enter (fn [{req :request :as ctx}]
              (let [body (get req :json-params)
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (r/writeDoc db id body)
                (assoc ctx :response (merge res (created body)))))}))


(defn updater [db]
  (interceptor
    {
     :name  :doc-updater
     :enter (fn [{req :request :as ctx}]
              (let [body (get req :body-params)
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (r/changeDoc db id body)
                (assoc ctx :response (merge res (accepted body)))))}))


(defn deleter [db]
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])]
                (r/deleteDoc db id)
                (assoc ctx :response (no-content "DELETED"))))}))



(def api-name "document")

(defn routes [db]
  (route/expand-routes
    #{
      [(format "/%s/:id" api-name) :put [(bp/body-params) (writer db)] :route-name :create]
      [(format "/%s/:id" api-name) :get [http/json-body (reader db)] :route-name :read]
      [(format "/%s/:id" api-name) :post [(bp/body-params) (updater db)] :route-name :update]
      [(format "/%s/:id" api-name) :delete (deleter db) :route-name :delete]
      }))


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


