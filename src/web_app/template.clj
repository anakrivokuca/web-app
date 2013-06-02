(ns web-app.template
  (:require [noir.session :as session])
  (:use [hiccup.core :only [html]]
        [hiccup.page :only [include-css]]))

(defn- home-selected 
  "Display main menu when Home page is selected."
  [user]
  [:ul
   [:li.selected
    [:a {:href "/"} "Home"]]
   [:li
    [:a {:href "/books"} "Books"]]
   (if (= "admin" user)
     [:li
      [:a {:href "/users"} "Users"]])])

(defn- books-selected 
  "Display main menu when Books page is selected."
  [user]
  [:ul
   [:li
    [:a {:href "/"} "Home"]]
   [:li.selected
    [:a {:href "/books"} "Books"]]
   (if (= "admin" user)
     [:li
      [:a {:href "/users"} "Users"]])])

(defn- users-selected 
  "Display main menu when Users page is selected."
  [user]
  [:ul
   [:li
    [:a {:href "/"} "Home"]]
   [:li
    [:a {:href "/books"} "Books"]]
   (if (= "admin" user)
     [:li.selected
      [:a {:href "/users"} "Users"]])])

(defn- register-login-selected 
  "Display main menu when pages from user menu are selected."
  [user]
  [:ul
   [:li
    [:a {:href "/"} "Home"]]
   [:li
    [:a {:href "/books"} "Books"]]
   (if (= "admin" user)
     [:li
      [:a {:href "/users"} "Users"]])]) 

(defn- menu 
  "Display main menu and mark selected page."
  [uri user]
  (cond
    (= uri "/") (home-selected user)
    (= uri "/books") (books-selected user) 
    (= uri "/users") (users-selected user)
    (or (= uri "/register") (= uri "/login") (= uri "/book")) (register-login-selected user))) 

(defn- user-logged-in 
  "Display user menu when user is logged in."
  [user]
  [:ul
   [:li
    [:a (str "Logged in as " user)]]
   [:li
    [:a {:href "/logout"} "Logout"]]])

(def ^:private home-users-books-selected
  "Display user menu when pages from main menu are selected."
  [:ul
   [:li
    [:a {:href "/register"} "Register"]]
   [:li
    [:a {:href "/login"} "Login"]]])

(def ^:private register-selected
  "Display user menu when Register page is selected."
  [:ul
   [:li.selected
    [:a {:href "/register"} "Register"]]
   [:li
    [:a {:href "/login"} "Login"]]])

(def ^:private login-selected
  "Display user menu when Login page is selected." 
  [:ul
   [:li
    [:a {:href "/register"} "Register"]]
   [:li.selected
    [:a {:href "/login"} "Login"]]])

(defn- user-menu 
  "Display user menu and mark selected page."
  [uri user]
  (cond
    user (user-logged-in user)
    (or (= uri "/") (= uri "/users") (= uri "/books") (= uri "/book")) home-users-books-selected
    (= uri "/register") register-selected
    (= uri "/login") login-selected))

(defn template-page 
  "Display header, main and user menu, body content and footer for all pages."
  [title uri content]
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
          [:img {:src "/images/clojure_quick_introduction.png" :alt "Books"}]]
         (menu uri user)]]
       
       [:div#body
        [:div.user-info
         (user-menu uri user)]
        content]
       
       [:div#footer
        [:p "Copyright &copy; 2013. All Rights Reserved"]]])))
