(defproject web-app "1.0.2"
  :description "My Clojure web application"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.8"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [congomongo "0.4.1"]
                 [lib-noir "0.5.0"]
                 [amalloy/mongo-session "0.0.2"]]
  :main web-app.core)
