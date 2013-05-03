(ns web-app.template)

(use 'hiccup.core)
(use 'hiccup.page)

(require '[noir.session :as session])


(defn menu [uri user]
  (cond
    (= uri "/") [:ul
                 [:li.selected
                  [:a {:href "/"} "Home"]]
                 (if (= "admin" user)
                   [:li
                    [:a {:href "/users"} "Users"]])]
         
    (= uri "/users") [:ul
                      [:li
                       [:a {:href "/"} "Home"]]
                      (if (= "admin" user)
                        [:li.selected
                         [:a {:href "/users"} "Users"]])]
         
    (or (= uri "/register") (= uri "/login")) [:ul
                                               [:li
                                                [:a {:href "/"} "Home"]]
                                               (if (= "admin" user)
                                                 [:li
                                                  [:a {:href "/users"} "Users"]])]
    )) 

(defn user-menu [uri user]
  (cond
    user [:ul
          [:li
           [:a (str "Logged in as " user)]]
          [:li
           [:a {:href "/logout"} "Logout"]]]
         
    (or (= uri "/") (= uri "/users")) [:ul
                                       [:li
                                        [:a {:href "/register"} "Register"]]
                                       [:li
                                        [:a {:href "/login"} "Login"]]]
         
    (= uri "/register") [:ul
                         [:li.selected
                          [:a {:href "/register"} "Register"]]
                         [:li
                          [:a {:href "/login"} "Login"]]]
    
    (= uri "/login") [:ul
                      [:li
                       [:a {:href "/register"} "Register"]]
                      [:li.selected
                       [:a {:href "/login"} "Login"]]]
    ))

(defn template-page [title uri content]
  (html 
    [:head
     [:meta {:charset "UTF-8"}] 
     [:title title]
     (include-css "/css/style.css")
     ;[:link {:rel "stylesheet" :href "css/style.css" :type "text/css"}]
     ]
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
