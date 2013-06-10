(ns web-app.index
  (:use [web-app.template :only [template-page]]
        [web-app.extract-data :only [active-agents]]
        [web-app.mongo :only [get-books]]))

(defn index-page [uri]
  (template-page 
    "Home page"
    uri
    [:div.body
       [:h1 {:style "border-bottom: 1px solid #ebebeb; padding: 10px;"} 
        "Clojure Web Application for IT Books"]
       [:h3 (if-not (zero? @active-agents)
              "Importing of data started. To check the number of imported books, refresh the page."
              "Processing data finished.")]
       [:p "Number of books imported: "
        [:b (count (get-books))]]
       [:p "Number of reviews imported: "
        [:b (reduce + 0 (for [book (get-books)] (count (:reviews book))))]]]))