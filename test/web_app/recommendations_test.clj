(ns web-app.recommendations-test
  (:use clojure.test
        web-app.recommendations))

(def book-critics {"1" {"1" 4 "2" 5}
                   "2" {"1" 4 "2" 5 "3" 1}
                   "3" {"1" 5 "2" 5 "3" 1}
                   "4" {"1" 3 "2" 0 "3" 1}
                   "5" {"3" 1}})

(deftest sim-pearson-test
  (is (= 1.0 (sim-pearson book-critics "1" "2")))
  (is (= 0.0 (sim-pearson book-critics "1" "3")))
  (is (= -1.0 (sim-pearson book-critics "1" "4")))
  (is (= -1 (sim-pearson book-critics "1" "5"))))

(deftest sim-euclidean-test
  (is (= 1.0 (sim-euclidean book-critics "1" "2")))
  (is (= 0.5 (sim-euclidean book-critics "1" "3")))
  (is (= 0.037037037037037035 (sim-euclidean book-critics "1" "4")))
  (is (= 0 (sim-euclidean book-critics "1" "5"))))