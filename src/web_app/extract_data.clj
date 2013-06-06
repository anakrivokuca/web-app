(ns web-app.extract_data
  (:require [hickory.core :as hickory]
            [hickory.select :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clj-time.format :as time-format])
  (:use [web-app.mongo :only [get-books insert-book delete-books]]))


(def page-links 
  "Prepare page links for links extraction."
  (for [i (range 1 2)]
    (str "http://www.goodreads.com/shelf/show/it?page=" i)))

(def extracted-book-links
  "Prepare links for data extraction."
  (for [link page-links]
    (let [content (s/select (s/child (s/class "bookTitle"))
                            (hickory/as-hickory (hickory/parse (slurp link))))]
      (map #(str "http://www.goodreads.com" %)
           (map :href
                (map :attrs content))))))

(defn get-json 
  "Extract microdata from specified link in json format."
  [link]
     (slurp (str "http://www.w3.org/2012/pyMicrodata/extract?format=json&uri=" link)))

(defn prepare-json 
  "Prepare json for further data extraction."
  [body]
  (json/read-str
    (string/replace
      (string/replace body "@" "") "http://schema.org/" "")
    :key-fn keyword))

(defn get-book-data 
  "Get section from json that contains book data."
  [json-text] 
  (second (:list (:md:item json-text))))

(defn get-review-data 
  "Get section from json that contains review data."
  [json-text link] 
  (if-let [review-data (:reviews (first (:list (:md:item json-text))))]
    review-data
    (first (remove #(= link (:id %)) (:graph json-text)))))

(defn get-data 
  "Get review data section from json if extraction fails."
  [link]
  (if-let [data (get-review-data (prepare-json (get-json link)) link)]
    data
    (recur link)))

(defn get-user-rating 
  "Extract user rating from page-link."
  [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :itemprop #(= % "ratingValue")))
                          (hickory/as-hickory (hickory/parse (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(def custom-formatter 
  (time-format/formatter "MMM dd, yy"))

(defn extract-review
  "Extract review data."
  [data page-link]
  (if-let [name (rest (clojure.string/split (:author data) #"\-"))]
    (assoc {}
           :authorId (first (clojure.string/split 
                         (apply str (last 
                                         (clojure.string/split (:author data)  #"\/"))) #"\-"))
           :author (apply str (interpose " " (map clojure.string/capitalize name)))
           :publishDate (.toDate (time-format/parse custom-formatter (:publishDate data)))
           :description (:reviewBody data)
           :ratingValue (get-user-rating page-link))))

(defn prepare-review-data 
  "Prepare review data for database insert."
  [links]
  (if (or (vector? links) (seq? links)) 
    (pmap #(extract-review (get-data %) %) (flatten links))
    (extract-review (get-data links) links)))

(defn get-image-link 
  "Extract book image from page-link."
  [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :property #(= % "og:image")))
                          (hickory/as-hickory (hickory/parse (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(defn extract-book 
  "Extract book data."
  [data page-link]
  (let [reviews (remove nil? (map #(prepare-review-data (:url %)) (:reviews data)))]
    (assoc {}
           :title (apply str (interpose " " (re-seq #"[A-Za-z0-9_]+" (:name data))))
           :image (get-image-link page-link)
           :author (:name (:author data))
           :isbn (:isbn data)
           :bookEdition (:bookEdition data) 
           :language (:inLanguage data)
           :bookFormatType (:bookFormatType data)
           :numberOfPages (:numberOfPages data)
           :awards (:awards data)
           :reviews reviews
           :ratingCount (count reviews)
           :ratingValue (double (/ (reduce + 0 (map #(Integer/valueOf (:ratingValue %)) reviews)) 
                                   (count reviews))))))

(defn prepare-book-data 
  "Prepare book data for database insert."
  [] 
  (remove nil? (pmap #(if-let [data (get-book-data (prepare-json (get-json %)))]
                        (extract-book data %)) 
          (flatten extracted-book-links))))

(defn insert-books []
  "Delete all books from database and insert extracted data" 
  (delete-books)
  (pmap insert-book (prepare-book-data)))

(defn process-data 
  "Insert prepared data to database and show information about processed data."
  []
  (println "Processing data started!\n")
  (time (doall (insert-books)))
  (println (str (count (get-books)) " books imported!\n"))
  (println (str (reduce + 0 (for [book (get-books)] (count (:reviews book)))) " reviews imported!\n"))
  (println "Processing data finished!\n"))