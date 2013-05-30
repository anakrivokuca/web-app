(ns web-app.books
  (:use [web-app.template :only [template-page]]
        [web-app.mongo :only [get-books]]
        [web-app.book :only [roundedRating]]))


(defn- books-table [books-fn] 
  [:div.body
   [:h2 "Books"] 
   [:div.form
    [:table
    (for [book (books-fn)]
       (identity  [:tr
                   [:td 
                    [:a {:href (str "/book/" (book :_id))}
                     [:img.bookSmallImg {:src (book :image) :alt (book :title)}]]]
                   [:td {:style "border-bottom: 1px solid #ebebeb; padding: 10px;"}
                    [:a {:href (str "/book/" (book :_id))}
                         (book :title)]
                    [:div 
                     (let [name (book :author)]
                          (if (vector? name)
                            (interpose ", " (book :author))
                            name))]
                    [:span {:class (str "rating-static rating-" (roundedRating book))}]]]))]]])

(defn books-page [uri]
  (template-page
    "Books page" 
    uri
    (books-table get-books)))