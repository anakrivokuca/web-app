(ns web-app.core)

(use '[ring.adapter.jetty :only (run-jetty)])
(use 'ring.middleware.reload)
(use 'ring.middleware.stacktrace) 
(use 'web-app.middleware)

(use 'compojure.core)
(use '[compojure.route :as route])

(use 'web-app.index)
(use 'web-app.register)
(use 'web-app.login)
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

#_(defn handler
[{:keys [uri query-string]}]
{:body (format "You requested %s with query %s" uri query-string)})

(defroutes handler
  (GET "/" [] (index-page "/"))
  (GET "/register" [] (register-page "/register"))
  (GET "/login" [] (login-page "/login"))
  (GET "/users/:id" [id]
      (format "You requested id %s" id))
  (route/resources "/")
  (route/not-found "Sorry, there's nothing here."))


(def app
  (-> #'handler
    (wrap-request-logging)
    (wrap-reload)
    ;(wrap-favicon)
    (wrap-stacktrace)))


(def server (run-jetty #'app {:port 8080 :join? false}))