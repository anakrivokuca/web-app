(ns web-app.login
  (:require [noir.session :as session]
            [ring.util.response :as response])
  (:use [hiccup.form :only [form-to label text-field password-field]]
        [web-app.template :only [template-page]]
        [web-app.mongo :only [get-user-by-username]]))


(defn- login-box
  "Show login form."
  []
  [:div.body
   [:h2 "Login"]
   [:div.form
    [:h4 "Enter your login credentials:"]
    [:div.error (session/flash-get :error)]
    (form-to [:post "/login"]
             [:table
              [:tr
               [:td (label :user "Username:")]
               [:td (text-field :user (session/flash-get :user))]]
              [:tr
               [:td (label :pass "Password:")]
               [:td (password-field :pass)]]
              [:tr
               [:td]
               [:td [:button {:type "submit"} "Log In"]]]])
    [:p "If you are not registered, you can register "
     [:a {:href "/register"} "here" ] "."]]])

(defn login-page
  "Show Login page."
  [uri]
  (template-page "Login page" uri (login-box)))

(defn verify-login-form
  "Verify all values entered in login form."
  [lower-user pass]
  (cond
   (nil? (get-user-by-username lower-user)) "Username does not exist."
   (not= pass (:pass (get-user-by-username lower-user)))
   "Password is not correct."
   :else true))

(defn do-login
  "If user credentials are correct, login."
  [user pass]
  (let [lower-user (clojure.string/lower-case user)
        error-msg (verify-login-form lower-user pass)]
    (if-not (string? error-msg)
      (do
        (session/put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :error error-msg)
        (session/flash-put! :user user)
        (response/redirect "/login")))))

(defn do-logout []
  (session/remove! :user)
  (response/redirect "/"))