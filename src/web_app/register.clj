(ns web-app.register)

(use 'web-app.template)
(use 'web-app.mongo) 

(use 'hiccup.form)

(require '[noir.session :as session])
(require '[ring.util.response :as response])


(defn register-box [] 
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
               [:td (submit-button "Register") 
                ;[:button {:type "submit"} "Register"]
                ]]])]])

(defn register-page [uri]
  (template-page
    "Register page"
    uri
    (register-box)))

(defn verify-register-form 
  [name email lower-user pass repeat-pass]
  (cond
    (> 3 (.length name)) "Name must be at least 3 characters long"
    (not= name (first (re-seq #"[A-Za-z0-9_]+" name))) "Name must be alphanumeric"
    (not (nil? (get-user-by-email email))) "Email address is already taken."
    (not (nil? (get-user-by-username lower-user))) "Username is already taken."
    (> 3 (.length lower-user)) "Username must be at least 3 characters long"
    (not= lower-user (first (re-seq #"[A-Za-z0-9_]+" lower-user))) "Username must be alphanumeric"
    (> 6 (.length pass)) "Password has to have more than 6 chars."
    (not= pass repeat-pass) "Password and confirmed password are not equal."
    :else true))

(defn do-register [name email user pass repeat-pass]
  (let [lower-user (.toLowerCase user)]
    (if (not (string? (verify-register-form name email lower-user pass repeat-pass)))
      (do
        (insert-user name email lower-user pass)
        (session/put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :error (verify-register-form name email lower-user pass repeat-pass))
        (session/flash-put! :name name)
        (session/flash-put! :email email)
        (session/flash-put! :user user)
        (response/redirect "/register"))
        )))