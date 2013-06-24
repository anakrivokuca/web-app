(ns web-app.users-test
  (:use clojure.test
        web-app.register
        web-app.login
        [web-app.mongo :only [insert-user get-user-by-username delete-user]])
  (:import com.mongodb.WriteResult))

(deftest insert-user-test
  (is (= 5 (count (insert-user "my-name"
                               "myemail@email.com"
                               "my-username"
                               "mypsswrd")))))

(deftest verify-register-form-test
  (let [name "name"
        shname "nm"
        lngname "thisisaveryverylongname"
        uname "username"
        lnguname "thisisalongusername"
        bname "$#%^$djc"
        pass "psswrd"
        shpass "pass"
        email "test@test.com"]
    (is (= true (verify-register-form name email uname pass pass)))
    (is (= "Name must be at least 3 characters long."
           (verify-register-form shname email uname pass pass)))
    (is (= "Name must be maximum 20 characters long."
           (verify-register-form lngname email uname pass pass)))
    (is (= "Name must be alphanumeric."
           (verify-register-form bname email uname pass pass)))
    (is (= "Email address is already taken."
           (verify-register-form name "myemail@email.com" uname pass pass)))
    (is (= "Username is already taken."
           (verify-register-form name email "my-username" pass pass)))
    (is (= "Username must be at least 3 characters long."
           (verify-register-form name email shname pass pass)))
    (is (= "Username must be maximum 14 characters long."
           (verify-register-form name email lnguname pass pass)))
    (is (= "Username must be alphanumeric."
           (verify-register-form name email bname pass pass)))
    (is (= "Password must have at least 6 chars."
           (verify-register-form name email uname shpass shpass)))
    (is (= "Password and confirmed password are not equal."
           (verify-register-form name email uname pass "differentpass")))))

(deftest verify-login-form-test
    (is (= true (verify-login-form "my-username" "mypsswrd")))
    (is (= "Username does not exist."
           (verify-login-form "uname" "mypsswrd")))
    (is (= "Password is not correct."
           (verify-login-form "my-username" "pass"))))

(deftest delete-user-test
  (is (= 1 (.getN (delete-user (:_id (get-user-by-username "my-username")))))))

(deftest test-user
  (insert-user-test)
  (verify-register-form-test)
  (verify-login-form-test)
  (delete-user-test))

(defn test-ns-hook []
  (test-user))