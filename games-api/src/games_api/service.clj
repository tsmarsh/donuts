(ns games-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
            [outpace.config :refer [defconfig]]))

(def noun (atom {}))

(def respond-hello
  (interceptor
    {
     :name  :respond-hello
     :enter (fn [ctx]
              (let [id (get-in ctx [:request :path-params :id])
                    n (get @noun id "world")]
                (assoc ctx :response
                           {:status 200 :body (format "Hello, %s!", n)})))
     }))

(defn write-noun [{:keys [body path-params] :as request}]
  (swap! noun assoc (:id path-params) (slurp body))
  {:status 200 :body "OK"})

(defn update-noun [{:keys [body path-params] :as request}]
  (swap! noun assoc (:id path-params) (slurp body))
  {:status 200 :body "OK"})

(defn delete-noun [{:keys [path-params] :as request}]
  (swap! noun dissoc (:id path-params))
  {:status 200 :body "OK"})

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))


(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [request (:request context)
                 response (ok context)]
             (assoc context :response response)))})

(def api-name "document")

(def routes
  (route/expand-routes
    #{
      [(format "/%s/:id" api-name) :put write-noun :route-name :create]
      [(format "/%s/:id" api-name) :get respond-hello :route-name :read]
      [(format "/%s/:id" api-name) :post update-noun :route-name :update]
      [(format "/%s/:id" api-name) :delete delete-noun :route-name :delete]
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


