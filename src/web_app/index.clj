(ns web-app.index)

(use 'web-app.template)

(defn index-page [uri]
  (template-page 
    uri
    [:div.body
       [:h1 "My Clojure Web Application"]
       [:p "This website is implemented in " [:a {:href "http://clojure.org/"} "Clojure"] " as an example of building Clojure web applications. Hiccup is a library for representing HTML in Clojure. It uses vectors to represent elements, and maps to represent an element's attributes. Do not hesitate to ask for help on this blog."]
       [:p "This website is implemented in " [:a {:href "http://clojure.org/"} "Clojure"] " as an example of building Clojure web applications. Hiccup is a library for representing HTML in Clojure. It uses vectors to represent elements, and maps to represent an element's attributes. Do not hesitate to ask for help on this blog."]]))