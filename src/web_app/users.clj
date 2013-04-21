(ns web-app.users)

(use 'web-app.template)
(use 'web-app.mongo) 
 
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))


(def users-table
  [:div.body
   [:h2 "Users"] 
   [:div.form
    [:table
     [:tr
      [:th "Name:"]
      [:th "Username:"]
      [:th "Email:"]]
     (for [i (dbg (get-users))]
       (identity [:tr
                  [:td (dbg (i :name))]
                  [:td (dbg (i :user))]
                  [:td (dbg (i :email))]]))]]])

(defn users-page [uri]
     (template-page
       "Users page" 
       uri 
       users-table))