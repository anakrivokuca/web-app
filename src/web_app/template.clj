(ns web-app.view)

(use 'hiccup.core)
(use 'hiccup.page)

(defn index-page []
  (html 
    [:head
     [:meta {:charset "UTF-8"}] 
     [:title "My home page"]
     (include-css "/css/style.css")
     ;[:link {:rel "stylesheet" :href "css/style.css" :type "text/css"}]
     ]
    [:body
     [:div#header
      [:div
       [:ul
        [:li.selected
         [:a {:href "index.html"} "home"]]
        [:li
         [:a {:href "blog.html"} "blog"]]
        [:li
         [:a {:href "contact.html"} "contact us"]]]]]
     [:div#body
      [:div.header
       [:img {:src "images/clojure_quick_introduction.png" :alt ""}]
       ]
      [:div.body
       [:h1 "My Clojure Web Application"]
       [:p "This website is implemented in " [:a {:href "http://clojure.org/"} "Clojure"] " as an example of building Clojure web applications. Hiccup is a library for representing HTML in Clojure. It uses vectors to represent elements, and maps to represent an element's attributes. Do not hesitate to ask for help on this blog."]
       [:p "This website is implemented in " [:a {:href "http://clojure.org/"} "Clojure"] " as an example of building Clojure web applications. Hiccup is a library for representing HTML in Clojure. It uses vectors to represent elements, and maps to represent an element's attributes. Do not hesitate to ask for help on this blog."]]]
     [:div#footer
      [:p "Copyright &copy; 2013. All Rights Reserved"]]]))
