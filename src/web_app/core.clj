(ns web-app.core
  (:require [compojure.route :as route]
            [noir.session :as session])
  
  (:use compojure.core
        [ring.adapter.jetty :only [run-jetty]]
        web-app.middleware
        [ring.middleware.reload :only [wrap-reload]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [ring.middleware.params :only [wrap-params]]
        [mongo-session.core :only [mongo-session]]
        [web-app.index :only [index-page]]
        [web-app.register :only [register-page do-register]]
        [web-app.login :only [login-page do-login do-logout]]
        [web-app.users :only [users-page do-delete-user]]))


(defroutes handler
  (GET "/" [] (index-page "/"))
  (GET "/register" [] (register-page "/register"))
  (POST "/register" [name email user pass repeat-pass]
        (do-register name email user pass repeat-pass))
  (GET "/login" [] (login-page "/login"))
  (POST "/login" [user pass] 
        (do-login user pass))
  (GET "/logout" [] (do-logout))
  (GET "/users" [] (users-page "/users"))
  (POST "/users/delete" [id]
      (do-delete-user (Integer/parseInt id)))
  (route/resources "/")
  (route/not-found "Sorry, there's nothing here."))

(def app
  (-> #'handler
    (wrap-request-logging)
    (wrap-reload)
    (wrap-params)
    (session/wrap-noir-flash)
    (session/wrap-noir-session {:store (mongo-session :sessions)})
    (wrap-stacktrace)))

(def server
  (run-jetty #'app {:port 8080 :join? false}))