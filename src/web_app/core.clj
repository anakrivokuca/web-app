(ns web-app.core)

(use '[ring.adapter.jetty :only (run-jetty)])
(use 'ring.middleware.reload)
(use 'ring.middleware.stacktrace)
(use 'ring.middleware.params)
(use 'web-app.middleware) 

(use 'compojure.core)
(use '[compojure.route :as route])

(use '[noir.session :as session])
;(use '[mongo-session.core :only [mongo-session]])

(use 'web-app.index)
(use 'web-app.register)
(use 'web-app.login)
(use 'web-app.users)
(use 'web-app.mongo)

#_(defn handler
[{:keys [uri query-string]}]
{:body (format "You requested %s with query %s" uri query-string)})

(defroutes handler
  (GET "/" [] (index-page "/"))
  (GET "/register" [] (register-page "/register"))
  (POST "/register" [name email user pass repeat-pass] ;{{:strs [name email user pass repeat-pass]} :form-params}
        (do-register name email user pass repeat-pass))
  (GET "/login" [] (login-page "/login"))
  (POST "/login" [user pass] 
        (do-login user pass))
  (GET "/users" [] (users-page "/users"))
  (GET "/users/:id" [id]
      (format "You requested id %s" id))
  (POST "/users/delete" [id]
      (do-delete-user (Integer/parseInt id)))
  (route/resources "/")
  (route/not-found "Sorry, there's nothing here."))


(def app
  (-> #'handler
    (wrap-request-logging)
    (wrap-reload)
    (wrap-params)
    ;(wrap-favicon)
    (session/wrap-noir-flash)
    (session/wrap-noir-session #_{:store (mongo-session :sessions)})
    (wrap-stacktrace)))

(def server
  (run-jetty #'app {:port 8080 :join? false}))