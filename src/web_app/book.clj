(ns web-app.book
  (:use [web-app.template :only [template-page]]
        [web-app.mongo :only [get-book-by-id]]))


(defn roundedRating [book]
  (let [rating (book :ratingValue)] 
    (cond
      (and (> rating 0) (< rating 0.25)) 0
      (and (> rating 0.25) (< rating 0.75)) 5
      (and (> rating 0.75) (< rating 1.25)) 10
      (and (> rating 1.25) (< rating 1.75)) 15
      (and (> rating 1.75) (< rating 2.25)) 20
      (and (> rating 2.25) (< rating 2.75)) 25
      (and (> rating 2.75) (< rating 3.25)) 30
      (and (> rating 3.25) (< rating 3.75)) 35
      (and (> rating 3.75) (< rating 4.25)) 40
      (and (> rating 4.25) (< rating 4.75)) 45
      :else 50)))

(defn- book-details [book]
  [:table
   [:tr
    [:td
     [:img {:src (book :image) :alt (book :title) :width "170"}]
     [:div {:align "center"}
      [:span "Book rating:"]
      [:span {:class (str "rating-static rating-" (roundedRating book))}]]]
    [:td
     [:table
      [:tr
       [:th "Title: "]
       [:td (book :title)]]
      [:tr 
       [:th "Author: "]
       [:td (let [name (book :author)]
              (if (vector? name)
                (interpose ", " (book :author))
                name))]]
      [:tr 
       [:th "ISBN: "]
       [:td (book :isbn)]]
      [:tr 
       [:th "Language: "]
       [:td (book :language)]]
      [:tr 
       [:th "Book Format Type: "]
       [:td (book :bookFormatType)]]
      [:tr 
       [:th "Number of Pages: "]
       [:td (book :numberOfPages)]]
      [:tr 
       [:th "Awards: "]
       [:td (book :awards)]]]]]])

(defn- review-list [book]
  [:div
   (for [review (book :reviews)]
     (identity [:table.review
                [:tr
                 [:th 
                  (review :author)
                  [:span {:class (str "rating-static rating-" (review :ratingValue) "0")}]]
                 [:th (review :publishDate)]]
                [:tr
                 [:td {:colspan "2"}
                  (review :description)]]]))])

(defn add-review []
  ;[:div "Rate this book:"]
                     #_[:ul.star-rating
                      [:li.current-rating {:style "width:0%;"} "Currently 0/5 Stars."]
                      [:li [:a.one-star {:href "#" :title "1 star out of 5"} "1"]]
                      [:li [:a.two-stars {:href "#" :title "2 stars out of 5"} "2"]]
                      [:li [:a.three-stars {:href "#" :title "3 stars out of 5"} "3"]]
                      [:li [:a.four-stars {:href "#" :title "4 stars out of 5"} "4"]]
                      [:li [:a.five-stars {:href "#" :title "5 stars out of 5"} "5"]]])

(defn- book-table [id]
  [:div.body
   (if-let [book (get-book-by-id id)]
     (identity [:div.book
                [:h2 (book :title)]
                [:div.form
                 (book-details book)
                 (review-list book)
                 (add-review)]])
      [:p "Book with specified id does not exist in database."])])

(defn book-page [uri id]
  (template-page
    "Book page" 
    uri
    (book-table (Integer/valueOf id))))