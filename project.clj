(defproject web-app "3.0.1"
  :description "My Clojure web application"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.1.8"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [congomongo "0.4.1"]
                 [lib-noir "0.6.4"]
                 [amalloy/mongo-session "0.0.2"]
                 [hickory "0.4.1"]
                 [org.clojure/data.json "0.2.2"]
                 [clj-time "0.5.1"]]
  :main web-app.core)