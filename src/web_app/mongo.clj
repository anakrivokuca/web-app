(ns web-app.mongo)

(use 'somnium.congomongo)
(use '[somnium.congomongo.config :only [*mongo-config*]])
;(import  '[org.jasypt.util.password StrongPasswordEncryptor])


(def conn 
  (make-connection "mydb"))

(set-connection! conn)

(defn verify-register-form 
  [name email lower-user pass repeat-pass]
  (cond
    (> 3 (.length name) 14) "Name must be 4-13 characters long"
    (not= name (first (re-seq #"[A-Za-z0-9_]+" name))) "Name must be alphanumeric"
    (not (nil? (fetch-one :users :where {:email email}))) "Email address is already taken."
    (not (nil? (fetch-one :users :where {:user lower-user}))) "Username is already taken."
    (> 3 (.length lower-user) 14) "Username must be 4-13 characters long"
    (not= lower-user (first (re-seq #"[A-Za-z0-9_]+" lower-user))) "Username must be alphanumeric"
    (> 6 (.length pass)) "Password has to have more than 6 chars."
    (not= pass repeat-pass) "Password and confirmed password are not equal."
    
    :else true))

(defn insert-user
  [name email lower-user pass]
  (insert! :users
                 {:name name
                  :email email
                  :user lower-user
                  :pass pass #_(.encryptPassword (StrongPasswordEncryptor.) pass)}))

(defn get-users []
   (fetch :users))

(defn get-user [username]
   (fetch-one :users :where {:user username}))