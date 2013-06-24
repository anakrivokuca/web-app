(ns web-app.mongo
  (:use somnium.congomongo))

(def conn (make-connection "mydb"))

(set-connection! conn)

(defn- next-seq
  [coll]
  "Generate :_id."
  (:seq (fetch-and-modify :sequences {:_id coll} {:$inc {:seq 1}}
                          :return-new? true :upsert? true)))

(defn- insert-with-id
  [coll values]
  (insert! coll (assoc values :_id (next-seq coll))))

(defn get-users
  []
  (fetch :users))

(defn get-user-by-username
  [username]
  (fetch-one :users :where {:user username}))

(defn get-user-by-email
  [email]
  (fetch-one :users :where {:email email}))

(defn insert-user
  [name email lower-user pass]
  (insert-with-id :users
                  {:name name
                   :email email
                   :user lower-user
                   :pass pass}))

(defn insert-inital-users
  []
  (if (empty? (get-users))
    (do
      (insert-user "admin" "admin@books.com" "admin" "admin")
      (insert-user "Ana" "anakrivokuca@gmail.com" "anakrivokuca" "1234567")
      (insert-user "Marko" "marko@gmail.com" "marko" "1234567"))))

(defn delete-user
  [id]
  (destroy! :users {:_id id}))

(defn get-books
  []
  (fetch :books))

(defn get-book-by-id
  [id]
  (fetch-one :books :where {:_id id}))

(defn get-books-by-title
  [title]
  (fetch :books
         :where {:title title}))

(defn get-books-by-author
  [author]
  (fetch :books
         :where {:author author}))

(defn get-books-by-isbn
  [isbn]
  (fetch :books
         :where {:isbn isbn}))

(defn insert-book
  [book]
  (insert-with-id :books book))

(defn update-book
  [book new-book]
  (update! :books book new-book))

(defn delete-books
  []
  (if (not= (fetch-count :books) 0)
    (let [ids (for [book (get-books)]
                (conj [] (:_id book)))]
      (doseq [id (range (apply min (flatten ids))
                        (inc (apply max (flatten ids))))]
        (destroy! :books {:_id id})))))