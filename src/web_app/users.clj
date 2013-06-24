(ns web-app.users
  (:require [noir.session :as session]
            [ring.util.response :as response])
  (:use [hiccup.form :only [form-to hidden-field submit-button]]
        [web-app.template :only [template-page]]
        [web-app.mongo :only [get-users delete-user]]))


(defn- users-table
  "List all users."
  [users-fn]
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
                   (form-to [:delete "/users"]
                            (hidden-field :id (i :_id))
                            (submit-button "Delete"))]]))]]])

(defn users-page
  "Show users page and enable deleting option if user is admin."
  [uri]
  (template-page
    "Users page"
    uri
    (if (= "admin" (session/get :user))
      (users-table get-users)
      [:div.body
       [:div.error "You are not allowed to see this page."]])))

(defn do-delete-user
  "Delete user."
  [id]
  (do
    (delete-user id)
    (response/redirect "/users")))