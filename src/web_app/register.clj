(ns web-app.register
  (:require [noir.session :as session]
            [ring.util.response :as response])
  
  (:use [hiccup.form :only [form-to label text-field email-field password-field submit-button]]
        [web-app.template :only [template-page]]
        [web-app.mongo :only [insert-user get-user-by-username get-user-by-email]]))


(defn- register-box 
  "Show register form."
  []
  [:div.body
   [:h2 "Register"] 
   [:div.form
    [:h4 "Fill in the form to register:"]
    [:div.error (session/flash-get :error)]
    (form-to [:post "/register"]
             [:table
              [:tr
               [:td (label :name "Name:")]
               [:td (text-field :name (session/flash-get :name))]]
              [:tr
               [:td (label :email "Email:")]
               [:td (email-field :email (session/flash-get :email))]]
              [:tr
               [:td (label :user "Username:")]
               [:td (text-field :user (session/flash-get :user))]]
              [:tr
               [:td (label :pass "Password:")]
               [:td (password-field :pass)]]
              [:tr
               [:td (label :repeat-pass "Confirm password:")]
               [:td (password-field :repeat-pass)]]
              [:tr
               [:td]
               [:td (submit-button "Register")]]])]])

(defn register-page 
  "Show Register page."
  [uri]
  (template-page
    "Register page"
    uri
    (register-box)))

(defn- verify-register-form 
  "Verify all values entered in register form." 
  [name email lower-user pass repeat-pass]
  (cond
    (> 3 (.length name)) "Name must be at least 3 characters long"
    (< 20 (.length name)) "Name must be maximum 20 characters long"
    (not= name (first (re-seq #"[A-Za-z0-9_]+" name))) "Name must be alphanumeric"
    (not (nil? (get-user-by-email email))) "Email address is already taken."
    (not (nil? (get-user-by-username lower-user))) "Username is already taken."
    (> 3 (.length lower-user)) "Username must be at least 3 characters long"
    (< 14 (.length lower-user)) "Username must be maximum 14 characters long"
    (not= lower-user (first (re-seq #"[A-Za-z0-9_]+" lower-user))) "Username must be alphanumeric"
    (> 6 (.length pass)) "Password has to have more than 6 chars."
    (not= pass repeat-pass) "Password and confirmed password are not equal."
    :else true))

(defn do-register 
  "If all user data are entered properly, add user to database."
  [name email user pass repeat-pass]
  (let [lower-user (clojure.string/lower-case user)
        error-msg (verify-register-form name email lower-user pass repeat-pass)]
    (if-not (string? error-msg)
      (do
        (insert-user name email lower-user pass)
        (session/put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :error error-msg)
        (session/flash-put! :name name)
        (session/flash-put! :email email)
        (session/flash-put! :user user)
        (response/redirect "/register")))))