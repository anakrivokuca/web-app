(ns web-app.login)

(use 'web-app.template)

(use 'hiccup.form)

(def login-box
  [:div.body
   [:h2 "Login"] 
   [:div.form
    [:h4 "Enter your login credentials:"]
    (form-to [:post "/login"]
             [:table
              [:tr
               [:td (label :user "Username:")]
               [:td (text-field :user)]]
              [:tr
               [:td (label :pass "Password:")]
               [:td (password-field :pass)]]
              [:tr
               [:td]
               [:td [:button {:type "submit"} "Log In"]]]
              [:tr
               [:td]
               [:td
                [:a {:href "/login/reset"} "Forgot your password?"]]]])]])

(defn login-page [uri]
     (template-page
       uri 
       login-box))

