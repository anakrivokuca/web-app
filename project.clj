(defproject web-app "1.0.2"
  :description "My Clojure web application"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.8"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [congomongo "0.4.1"]
                 [lib-noir "0.5.0"]
                 [amalloy/mongo-session "0.0.2"]
                 [hickory "0.4.0"]
                 [org.clojure/data.json "0.2.1"]]
  :main web-app.core)
