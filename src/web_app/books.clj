(ns web-app.books
  (:use [hiccup.form :only [form-to text-field submit-button]]
        [web-app.template :only [template-page]]
        [web-app.mongo :only [get-books get-books-by-title
                              get-books-by-author get-books-by-isbn]]
        [web-app.book :only [round-static-rating]]))


(defn- book-search-box
  "Show book search form."
  []
  [:div.book
   [:h2 "Find book:"]
   (form-to [:post "/books"]
            [:table
             [:tr
              [:th {:style "width: 400px;"}
               "Search by Book Title, Author, or ISBN: "]]
             [:tr
              [:td
               (text-field :criteria)
               (submit-button "Search")]]])])

(defn- list-books
  "List all books."
  [books]
  [:div
   [:h2 "Books"]
   [:div.form
    [:table
     (for [book books]
       (identity [:tr
                  [:td
                   [:a {:href (str "/book/" (book :_id))}
                    [:img.bookSmallImg {:src (book :image)
                                        :alt (book :title)}]]]
                  [:td
                   {:style "border-bottom: 1px solid #ebebeb; padding: 10px;"}
                   [:a {:href (str "/book/" (book :_id))}
                    (book :title)]
                   [:div
                    (let [name (book :author)]
                      (if (vector? name)
                        (interpose ", " (book :author))
                        name))]
                   [:span {:class (str "rating-static rating-"
                                       (round-static-rating book))}]]]))]]])

(defn- pagination
  [criteria page last]
  (if-not (= 0 last)
    [:p
     (if-not (= 1 page)
       [:span
        [:a {:href (str "/books/" criteria "&1")} "<< First"] " "
        (if-not (= 2 page)
          [:a {:href (str "/books/" criteria "&" (- page 1))} "< Previous"])])
     (if-not (= 1 last)
       [:span " " [:b (str page " of " last " pages")] " "])
     (if-not (= last page)
       [:span
        [:a {:href (str "/books/" criteria "&" (+ page 1))} "Next >"] " "
        (if-not (= (- last 1) page)
          [:a {:href (str "/books/" criteria "&" last)} "Last >>"])])]))

(defn- books-layout
  "Show book search form, pagination and list books."
  [books-fn criteria page]
  [:div.body
   (book-search-box)
   (let [books (take 10 (drop (* 10 (- page 1)) books-fn))]
     (if-not (empty? books)
       [:div
        [:div {:style "float: right;"}
         (pagination criteria page
                     (let [number-of-pages (/ (count books-fn) 10)]
                       (if (ratio? number-of-pages)
                         (int (inc (Math/floor (double number-of-pages))))
                         number-of-pages)))]
        (list-books books)]
       (if (= criteria "all")
         [:p "There are no books in the database."]
         [:p "There are no books with specified search criteria."])))])

(defn- get-books-by-search-criteria
  "Find books by book title, author or ISBN."
  [criteria]
  (cond
    (not-empty (get-books-by-title criteria)) (get-books-by-title criteria)
    (not-empty (get-books-by-author criteria)) (get-books-by-author criteria)
    (not-empty (get-books-by-isbn criteria)) (get-books-by-isbn criteria)
    :else nil))

(defn books-page
  "Show Books page depending on search criteria."
  ([uri] (template-page
           "Books page"
           uri
           (books-layout (get-books) "all" 1)))
  ([uri criteria page] (template-page
                         "Books page"
                         uri
                         (books-layout
                          (if (= criteria "all")
                            (get-books)
                            (get-books-by-search-criteria criteria))
                          criteria page))))