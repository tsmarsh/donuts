(ns games-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
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


(defn read-doc [db]
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])
                    n (get @db id "world")]
                (assoc ctx :response (ok (format "Hello, %s!", n)))))}))


(defn write-doc [db]
  (interceptor
    {
     :name  :doc-writer
     :enter (fn [{req :request :as ctx}]
              (let [body (slurp (:body req))
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (swap! db assoc id body)
                (assoc ctx :response (merge res (created body)))))}))


(defn update-doc [db]
  (interceptor
    {
     :name  :doc-updater
     :enter (fn [{req :request :as ctx}]
              (let [body (slurp (:body req))
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (swap! db assoc id body)
                (assoc ctx :response (merge res (accepted body)))))}))


(defn delete-doc [db]
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])]
                (swap! db dissoc id)
                (assoc ctx :response (no-content "DELETED"))))}))



(def api-name "document")

(defn routes [db]
  (route/expand-routes
    #{
      [(format "/%s/:id" api-name) :put (write-doc db) :route-name :create]
      [(format "/%s/:id" api-name) :get (read-doc db) :route-name :read]
      [(format "/%s/:id" api-name) :post (update-doc db) :route-name :update]
      [(format "/%s/:id" api-name) :delete (delete-doc db) :route-name :delete]
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


