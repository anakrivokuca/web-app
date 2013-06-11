(ns web-app.book
  (:require [noir.session :as session]
            [ring.util.response :as response]
            [clj-time.core :as time-core]
            [clj-time.format :as time-format])
  (:use [hiccup.form :only [form-to label text-area submit-button]]
        [web-app.template :only [template-page]]
        [web-app.mongo :only [get-book-by-id update-book get-user-by-username]]
        [web-app.extract-data :only [custom-formatter]]
        [web-app.recommendations :only [get-similar-books-pearson get-similar-books-euclidean]]))

(defn round-static-rating 
  "Prepare static rating stars for dislaying 1/2 and 1 star ratings from all users."
  [book]
  (if-let [rating (dbg (:ratingValue book))] 
    (dbg (cond
      (and (>= rating 0.0) (< rating 0.25)) "00"
      (and (>= rating 0.25) (< rating 0.75)) "5"
      (and (>= rating 0.75) (< rating 1.25)) "10"
      (and (>= rating 1.25) (< rating 1.75)) "15"
      (and (>= rating 1.75) (< rating 2.25)) "20"
      (and (>= rating 2.25) (< rating 2.75)) "25"
      (and (>= rating 2.75) (< rating 3.25)) "30"
      (and (>= rating 3.25) (< rating 3.75)) "35"
      (and (>= rating 3.75) (< rating 4.25)) "40"
      (and (>= rating 4.25) (< rating 4.75)) "45"
      :else "50"))))

(defn- book-details 
  "Show book details."
  [book]
  [:table
   [:tr
    [:td
     [:img {:src (book :image) :alt (book :title) :width "170"}]
     [:div {:align "center"}
      [:span "Book rating:"]
      [:span {:class (str "rating-static rating-" (round-static-rating book))}]]]
    [:td
     [:table
      [:tr
       [:th "Title: "]
       [:td (str (book :title) 
                 (if-let [book-edition (book :bookEdition)] 
                   (str ", " book-edition)))]]
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

(defn- similar-books-list
  "Show similar books according to user ratings."
  [id]
  (let [similar-books (take 15 (get-similar-books-pearson id))]
    (if (not= 0 (count similar-books)) 
      [:div
     [:h4 "People who liked this book also liked:"]
     (for [book-id (keys similar-books)] 
       (let [book (get-book-by-id book-id)] 
         [:a {:href (str "/book/" (book :_id))}
                    [:img.bookSmallImg {:src (book :image) :alt (book :title)}]]))])))

#_(defn- similar-books-list-euclidean
  "Show similar books according to user ratings."
  [id]
  (let [similar-books (take 15 (get-similar-books-euclidean id))]
    (if (not= 0 (count similar-books)) 
      [:div
     [:h4 "People who liked this book also liked (Euclidean):"]
     (for [book-id (keys similar-books)] 
       (let [book (get-book-by-id book-id)] 
         [:a {:href (str "/book/" (book :_id))}
                    [:img.bookSmallImg {:src (book :image) :alt (book :title)}]]))])))

(defn- review-list 
  "List all book reviews."
  [book]
  [:div
   [:h4 "Reviews:"] 
   (for [review (reverse (sort-by :publishDate (book :reviews)))]
     (identity [:table {:style "width: 930px;"}
                [:tr
                 [:th 
                  (review :author)
                  [:span {:class (str "rating-static rating-" (review :ratingValue) "0")}]]
                 [:th {:style "text-align: right;"} (time-format/unparse custom-formatter (new org.joda.time.DateTime (review :publishDate) (org.joda.time.DateTimeZone/forID "UTC")))]]
                [:tr
                 [:td {:colspan "2"}
                  (review :description)]]]))])

(defn- calculate-dynamic-star-percentage 
  "Prepare dynamic rating stars for displaying user rating."
  [new-rating]
  (condp = new-rating
    nil 0
    0 0
    1 20
    2 40
    3 60
    4 80
    100))

(defn- show-dynamic-rating-stars 
  "Show user rating and allow user to rate a book."
  [id]
  [:ul.star-rating
   (let [new-rating (session/get :rating)]
     [:li.current-rating {:style (str "width:" (calculate-dynamic-star-percentage new-rating) "%;")}
      (str "Currently " new-rating "/5 Stars.")])
   [:li [:a.one-star {:href (str "/rate/" id "&1") :title "1 star out of 5"} "1"]]
   [:li [:a.two-stars {:href (str "/rate/" id "&2") :title "2 stars out of 5"} "2"]]
   [:li [:a.three-stars {:href (str "/rate/" id "&3") :title "3 stars out of 5"} "3"]]
   [:li [:a.four-stars {:href (str "/rate/" id "&4") :title "4 stars out of 5"} "4"]]
   [:li [:a.five-stars {:href (str "/rate/" id "&5") :title "5 stars out of 5"} "5"]]])

(defn- add-review-box 
  "Show dynamic rating stars and form for adding comments."
  [id user]
  [:div.form
   [:a {:name "addComment"}]
   [:h4 "Add new review:"]
  (form-to [:post "/addreview"]
           [:table
            [:tr
             [:td "Rate this book: "]
             [:td (show-dynamic-rating-stars id)]]
            [:tr
             [:td (label :comment "Comment: ")]
             [:td (text-area :comment)]]
            [:tr
             [:td]
             [:td (submit-button "Add")]]])])

(defn do-add-review 
  "Calculate new rating count and value and save comment."
  [comment new-rating]
  (let [book (session/get :book)
        user (session/get :user)
        new-book (merge book 
                        {:ratingCount (inc (book :ratingCount))}
                        {:ratingValue (double (/ (+ (book :ratingValue) new-rating) 2))}
                        {:reviews (conj (book :reviews)
                                        (assoc {}
                                               :author user
                                               :authorId (str (:_id (get-user-by-username user)))
                                               :publishDate (.toDate (time-core/now))
                                               :description comment
                                               :ratingValue new-rating))})]
    (do
      (update-book book new-book)
      (session/remove! :id)
      (session/remove! :rating)
      (response/redirect (str "/book/" (book :_id))))))

(defn- book-table 
  "Display book details and book reviews.
  If user is logged in, display form for adding reviews."
  [id]
  [:div.body
   (if-let [book (get-book-by-id id)]
     (do
       (session/put! :book book)
       (identity [:div.book
                  [:h2 (book :title)]
                  [:div.form
                   (book-details book)
                   (similar-books-list id)
                   ;(similar-books-list-euclidean id)
                   (review-list book)
                   (if-let [user (session/get :user)]
                     (add-review-box id user))]]))
     [:p "Book with specified id does not exist in database."])])

(defn book-page 
  "Show Book page." 
  [uri id]
  (template-page
    "Book page" 
    uri
    (book-table (Integer/valueOf id))))