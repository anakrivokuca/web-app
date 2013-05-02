(ns web-app.template)

(use 'hiccup.core)
(use 'hiccup.page)

(require '[noir.session :as session])

(defn template-page [title uri content]
  (html 
    [:head
     [:meta {:charset "UTF-8"}] 
     [:title title]
     (include-css "/css/style.css")
     ;[:link {:rel "stylesheet" :href "css/style.css" :type "text/css"}]
     ]
    [:body
     [:div#header
      [:div
       [:a.logo
       [:img {:src "images/clojure_quick_introduction.png" :alt ""}]
       ]
       
        (cond
          (= uri "/") [:ul 
                       [:li.selected
                        [:a {:href "/"} "home"]]
                       [:li
                        [:a {:href "/register"} "register"]]
                       [:li
                        [:a {:href "/login"} "login"]]
                       (if (= "admin" (session/get :user)) 
                         [:li
                             [:a {:href "/users"} "users"]])
                       ]

          (= uri "/register") [:ul 
                               [:li
                                [:a {:href "/"} "home"]]
                               [:li.selected
                                [:a {:href "/register"} "register"]]
                               [:li
                                [:a {:href "/login"} "login"]]
                               (if (= "admin" (session/get :user))
                                 [:li
                                  [:a {:href "/users"} "users"]])
                               ]
          
          (= uri "/login") [:ul 
                            [:li
                             [:a {:href "/"} "home"]]
                            [:li
                             [:a {:href "/register"} "register"]]
                            [:li.selected
                             [:a {:href "/login"} "login"]]
                            (if (= "admin" (session/get :user))
                              [:li
                               [:a {:href "/users"} "users"]])
                            ]
          
          (= uri "/users") [:ul 
                            [:li
                             [:a {:href "/"} "home"]]
                            [:li
                             [:a {:href "/register"} "register"]]
                            [:li
                             [:a {:href "/login"} "login"]]
                            (if (= "admin" (session/get :user))
                              [:li.selected
                               [:a {:href "/users"} "users"]])
                            ])]]
     [:div#body
      content]
     [:div#footer
      [:p "Copyright &copy; 2013. All Rights Reserved"]]]))
