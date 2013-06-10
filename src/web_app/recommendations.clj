(ns web-app.recommendations
  (:use [web-app.mongo :only [get-books]]))

(def ^:private book-critics
  "Gets all books ids with their user ratings."
  (let [sq (for [book (get-books)]
             (conj [] (:_id book)
                   (for [review (:reviews book)]
                     (conj []
                           (:authorId review)
                           (Integer/parseInt (:ratingValue review))))))]
    (zipmap (map first sq)
            (flatten (map #(map (fn [s] (into {} s)) (map set (rest %))) sq)))))

(defn- get-shared-prefs 
  "Gets shared user ratings between the specified books."
  [prefs book-critic1 book-critic2]
  (clojure.set/intersection 
    (set (keys (prefs book-critic1))) 
    (set (keys (prefs book-critic2)))))

(defn sim-pearson 
  "Calculates the pearson correlation score based on shared ratings between the
   specified books."
  [prefs critic1 critic2]
  (let [shared (get-shared-prefs prefs critic1 critic2)
        length (float (count shared))]
    (if (== 0 length)
      -1
      (let [ratings1 (map #(get-in prefs [critic1 %]) shared)
            ratings2 (map #(get-in prefs [critic2 %]) shared)
            sum1 (reduce + 0 ratings1)
            sum2 (reduce + 0 ratings2)
            sum1sq (reduce + 0 (map #(Math/pow % 2) ratings1))
            sum2sq (reduce + 0 (map #(Math/pow % 2) ratings2))
            psum (reduce + 0 (map * ratings1 ratings2))
            numerator (- psum (/ (* sum1 sum2) length))
            denominator (Math/sqrt (* (- sum1sq (/ (Math/pow sum1 2) length)) 
                                      (- sum2sq (/ (Math/pow sum2 2) length))))]
        (if (== 0 denominator)
          0.0
          (/ numerator denominator))))))

(defn- sort-book-scores [book-scores]
  (sorted-map-by (fn [key1 key2]
                   (compare [(book-scores key2) key2]
                            [(book-scores key1) key1]))))

(defn get-similar-books-pearson 
  "Gets sorted list of books and their similarity scores calculated with Pearson."
  [book-id]
  (let [books-ids (keys book-critics)
        similarity-scores (for [book-id2 books-ids]
                            (if-not (= book-id2 book-id)
                              (sim-pearson book-critics book-id book-id2)))
        m (zipmap books-ids similarity-scores)]
    (filter #(if-let [value (val %)]
               (>= value 0)) 
            (into (sort-book-scores m)  m))))

(defn sim-euclidean 
  "Calculates the euclidean distance score based on shared ratings between the
   specified books."
  [prefs critic1 critic2]
  (let [shared (get-shared-prefs prefs critic1 critic2)
        denominator (reduce #(+ %1 (Math/pow (- (get-in prefs [critic1 %2])
                                                (get-in prefs [critic2 %2])) 2))
                            0 shared)]
    (if (== (count shared) 0)
      0
      (/ 1 (+ 1 denominator)))))

(defn get-similar-books-euclidean 
  "Gets sorted list of books and their similarity scores calculated with Euclidean."
  [book-id]
  (let [books-ids (keys book-critics)
        similarity-scores (for [book-id2 books-ids]
                            (if-not (= book-id2 book-id)
                              (sim-euclidean book-critics book-id book-id2)))
        m (zipmap books-ids similarity-scores)]
    (filter #(if-let [value (val %)]
               (>= value 0.5))
            (into (sort-book-scores m) m))))