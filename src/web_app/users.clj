(ns web-app.users)

(use 'web-app.template)
(use 'web-app.mongo)

(use 'hiccup.form)

(require '[ring.util.response :as response])


(defn users-table [users-fn] 
  [:div.body
   [:h2 "Users"] 
   [:div.form
    
    [:table
     [:tr
      [:th "Name:"]
      [:th "Username:"]
      [:th "Email:"]
      [:th "Action:"]]
     (for [i (users-fn)]
       (identity [:tr
                  [:td (i :name)]
                  [:td (i :user)]
                  [:td (i :email)]
                  [:td 
                   (form-to [:post "/users/delete"]
                            (hidden-field :id (i :_id))
                            (submit-button "Delete"))]]))]]])

(defn users-page [uri]
  (template-page
    "Users page" 
    uri 
    (users-table get-users)))

(defn do-delete-user [id]
  (do
    (delete-user id)
    (response/redirect "/users")))