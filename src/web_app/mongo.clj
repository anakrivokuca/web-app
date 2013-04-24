(ns web-app.mongo)

(use 'somnium.congomongo)
(use '[somnium.congomongo.config :only [*mongo-config*]])
;(import  '[org.jasypt.util.password StrongPasswordEncryptor])


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
                   :pass pass #_(.encryptPassword (StrongPasswordEncryptor.) pass)}))

(defn delete-user [id]
  (destroy! :users {:_id id}))