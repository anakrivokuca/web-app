(ns web-app.core
  (:require [compojure.route :as route]
            [noir.session :as session]
            [ring.util.response :as response])
  
  (:use [compojure.core :only [defroutes GET POST DELETE]]
        [ring.adapter.jetty :only [run-jetty]]
        ;web-app.middleware
        [ring.middleware.reload :only [wrap-reload]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [ring.middleware.params :only [wrap-params]]
        [mongo-session.core :only [mongo-session]]
        [web-app.index :only [index-page]]
        [web-app.register :only [register-page do-register]]
        [web-app.login :only [login-page do-login do-logout]]
        [web-app.users :only [users-page do-delete-user]]
        [web-app.books :only [books-page]]
        [web-app.book :only [book-page do-add-review]]
        [web-app.extract-data :only [process-data]]
        [web-app.mongo :only [insert-inital-users]]))


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
  (DELETE "/users" [id]
      (do-delete-user (Integer/valueOf id)))
  (GET "/books" [] (books-page "/books"))
  (POST "/books" [criteria] 
        (books-page "/books" criteria 1))
  (GET "/book/:id" [id] (book-page "/book" id))
  (GET "/books/:criteria&:page" [criteria page]
      (books-page "/books" criteria (Integer/valueOf page)))
  (GET "/rate/:id&:rating" [id rating] 
       (session/put! :rating (Integer/valueOf rating))
       (response/redirect (str "/book/" id "#addComment")))
  (POST "/addreview" [comment]
        (do-add-review comment (session/get :rating)))
  (route/resources "/")
  (route/not-found "Sorry, there's nothing here."))

(def app
  (-> #'handler
    ;(wrap-request-logging)
    (wrap-reload)
    (wrap-params)
    (session/wrap-noir-flash)
    (session/wrap-noir-session {:store (mongo-session :sessions)})
    (wrap-stacktrace)))

(defn start-server [] 
  (run-jetty #'app {:port 8080 :join? false})
  (println "\nWelcome to the web-app. Browse to http://localhost:8080 to get started!"))

(defn -main [& args]
  (insert-inital-users) 
  (process-data)
  (start-server))