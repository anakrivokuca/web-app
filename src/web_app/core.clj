(ns web-app.core)

(use '[ring.adapter.jetty :only (run-jetty)])
(use 'ring.middleware.reload)
(use 'ring.middleware.stacktrace) 
(use 'web-app.middleware)

(use 'compojure.core)
(use '[compojure.route :as route])

(use 'web-app.view)

#_(defn handler
[{:keys [uri query-string]}]
{:body (format "You requested %s with query %s" uri query-string)})

(defroutes handler
  (GET "/" [] (index-page))
  (GET "/users/:id" [id]
      (format "You requested id %s" id))
  (route/resources "/")
  (route/not-found "Sorry, there's nothing here."))


(def app
  (-> #'handler
    (wrap-request-logging)
    (wrap-reload)
    (wrap-stacktrace)))


(def server (run-jetty #'app {:port 8080 :join? false}))