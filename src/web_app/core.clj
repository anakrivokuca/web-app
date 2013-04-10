(ns web-app.core)
(use '[ring.adapter.jetty :only (run-jetty)])
(use 'ring.middleware.reload)
(use 'ring.middleware.stacktrace) 
(use 'web-app.middleware)

(defn handler
[{:keys [uri query-string]}]
{:body (format "You requested %s with query %s" uri query-string)})

(def app
  (-> #'handler
    (wrap-request-logging)
    (wrap-reload)
    (wrap-stacktrace)))


(def server (run-jetty #'app {:port 8080 :join? false}))