(ns games-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
            [outpace.config :refer [defconfig]]))

(def noun (atom {}))

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


(def read-doc
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])
                    n (get @noun id "world")]
                (assoc ctx :response (ok (format "Hello, %s!", n)))))}))


(def write-doc
  (interceptor
    {
     :name  :doc-writer
     :enter (fn [{req :request :as ctx}]
              (let [body (slurp (:body req))
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (swap! noun assoc id body)
                (assoc ctx :response (merge res (created body)))))}))


(def update-doc
  (interceptor
    {
     :name  :doc-updater
     :enter (fn [{req :request :as ctx}]
              (let [body (slurp (:body req))
                    id (get-in req [:path-params :id])
                    res (:response ctx)]
                (swap! noun assoc id body)
                (assoc ctx :response (merge res (accepted body)))))}))


(def delete-doc
  (interceptor
    {
     :name  :doc-reader
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])
                    n (get @noun id "world")]
                (assoc ctx :response (no-content "DELETED"))))}))



(def api-name "document")

(def routes
  (route/expand-routes
    #{
      [(format "/%s/:id" api-name) :put write-doc :route-name :create]
      [(format "/%s/:id" api-name) :get read-doc :route-name :read]
      [(format "/%s/:id" api-name) :post update-doc :route-name :update]
      [(format "/%s/:id" api-name) :delete delete-doc :route-name :delete]
      }))


(def service-map
  {::http/routes routes
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


