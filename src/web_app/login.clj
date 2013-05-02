(ns web-app.login)

(use 'web-app.template)
(use 'web-app.mongo)

(use 'hiccup.form)

(require '[noir.session :as session])
(require '[ring.util.response :as response])


(defn login-box [] 
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
               [:td [:button {:type "submit"} "Log In"]]]
              [:tr
               [:td]
               [:td
                [:a {:href "/login/reset"} "Forgot your password?"]]]])
    [:p "If you are not registered, you can register " [:a {:href "/register"} "here" ] "."]]])

(defn login-page [uri]
  (template-page
    "Login page"
    uri
    (login-box)))

(defn verify-login-form 
  [lower-user pass]
  (cond
    (nil? (get-user-by-username lower-user)) "Username does not exist."
    (not= pass (:pass (get-user-by-username lower-user))) "Password is not correct."
    :else true))

(defn do-login [user pass]
  (let [lower-user (.toLowerCase user)]
    (if (not (string? (verify-login-form lower-user pass)))
      (do 
        (session/put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :error (verify-login-form lower-user pass))
        (session/flash-put! :user user)
        (response/redirect "/login"))
      )))