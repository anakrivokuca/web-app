(ns web-app.register)

(use 'web-app.template)
(use 'web-app.mongo) 

(use 'hiccup.form)

(require '[ring.util.response :as response])


(def register-box
  [:div.body
   [:h2 "Register"] 
   [:div.form
    [:h4 "Fill in the form to register:"]
    (form-to [:post "/register"]
             [:table
              [:tr
               [:td (label :name "Name:")]
               [:td (text-field :name)]]
              [:tr
               [:td (label :email "Email:")]
               [:td (email-field :email)]]
              [:tr
               [:td (label :user "Username:")]
               [:td (text-field :user)]]
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
    register-box))

(defn verify-register-form 
  [name email lower-user pass repeat-pass]
  (cond
    (> 3 (.length name) 14) "Name must be 4-13 characters long"
    (not= name (first (re-seq #"[A-Za-z0-9_]+" name))) "Name must be alphanumeric"
    (not (nil? (get-user-by-email email))) "Email address is already taken."
    (not (nil? (get-user-by-username lower-user))) "Username is already taken."
    (> 3 (.length lower-user) 14) "Username must be 4-13 characters long"
    (not= lower-user (first (re-seq #"[A-Za-z0-9_]+" lower-user))) "Username must be alphanumeric"
    (> 6 (.length pass)) "Password has to have more than 6 chars."
    (not= pass repeat-pass) "Password and confirmed password are not equal."
    :else true))

(defn do-register [name email user pass repeat-pass]
  (let [lower-user (.toLowerCase user)]
    (if (verify-register-form name email lower-user pass repeat-pass)
      (do
        (insert-user name email lower-user pass) ;)
        ;(session/put! :user lower-user)
        (response/redirect "/"))
        )))