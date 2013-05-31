(ns web-app.mongo
  (:use somnium.congomongo))

(def conn 
  (make-connection "mydb"))

(set-connection! conn)

;generate :_id
(defn- next-seq [coll]
  (:seq (fetch-and-modify :sequences {:_id coll} {:$inc {:seq 1}}
                             :return-new? true :upsert? true)))

(defn- insert-with-id [coll values]
  (insert! coll (assoc values :_id (next-seq coll))))

(defn get-users []
   (fetch :users))

(defn get-user-by-username [username] 
  (fetch-one :users :where {:user username}))

(defn get-user-by-email [email] 
  (fetch-one :users :where {:email email}))

(defn insert-user
  [name email lower-user pass]
  (insert-with-id :users 
                  {:name name
                   :email email
                   :user lower-user
                   :pass pass}))

(defn delete-user [id]
  (destroy! :users {:_id id}))

(defn get-books []
   (fetch :books))

(defn get-book-by-id [id]
   (fetch-one :books :where {:_id id}))

(defn insert-book [book]
  (insert-with-id :books book))

(defn update-book [book new-book]
  (update! :books book new-book))

(defn delete-books []
  (if (not= (fetch-count :books) 0)
    (let [ids (for [book (get-books)]
                (conj [] (:_id book)))]
      (doseq  [id (range (apply min (flatten ids)) (inc (apply max (flatten ids))))]
        (destroy! :books {:_id id})))))
