(ns web-app.template
  (:require [noir.session :as session])
  (:use [hiccup.core :only [html]]
        [hiccup.page :only [include-css]]))

(defn- home-selected [user]
  [:ul
   [:li.selected
    [:a {:href "/"} "Home"]]
   (if (= "admin" user)
     [:li
      [:a {:href "/users"} "Users"]])])

(defn- users-selected [user]
  [:ul
   [:li
    [:a {:href "/"} "Home"]]
   (if (= "admin" user)
     [:li.selected
      [:a {:href "/users"} "Users"]])])

(defn- register-login-selected [user]
  [:ul
   [:li
    [:a {:href "/"} "Home"]]
   (if (= "admin" user)
     [:li
      [:a {:href "/users"} "Users"]])]) 

(defn- menu [uri user]
  (cond
    (= uri "/") (home-selected user)
    (= uri "/users") (users-selected user)
    (or (= uri "/register") (= uri "/login")) (register-login-selected user))) 

(defn- user-logged-in [user]
  [:ul
   [:li
    [:a (str "Logged in as " user)]]
   [:li
    [:a {:href "/logout"} "Logout"]]])

(def ^:private home-users-selected
  [:ul
   [:li
    [:a {:href "/register"} "Register"]]
   [:li
    [:a {:href "/login"} "Login"]]])

(def ^:private register-selected
  [:ul
   [:li.selected
    [:a {:href "/register"} "Register"]]
   [:li
    [:a {:href "/login"} "Login"]]])

(def ^:private login-selected
  [:ul
   [:li
    [:a {:href "/register"} "Register"]]
   [:li.selected
    [:a {:href "/login"} "Login"]]])

(defn- user-menu [uri user]
  (cond
    user (user-logged-in user)
    (or (= uri "/") (= uri "/users")) home-users-selected
    (= uri "/register") register-selected
    (= uri "/login") login-selected))

(defn template-page [title uri content]
  (html 
    [:head
     [:meta {:charset "UTF-8"}] 
     [:title title]
     (include-css "/css/style.css")]
    (let [user (session/get :user)]
      [:body
       
       [:div#header
        [:div
         [:a.logo
          [:img {:src "images/clojure_quick_introduction.png" :alt ""}]]
         (menu uri user)]]
       
       [:div#body
        [:div.user-info
         (user-menu uri user)]
        content]
       
       [:div#footer
        [:p "Copyright &copy; 2013. All Rights Reserved"]]])))
