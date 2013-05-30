(ns web-app.extract_data
  (:require [hickory.select :as s]
            [clojure.string :as string]
            [clojure.data.json :as json])
  (:use hickory.core
        [web-app.mongo :only [get-books insert-book delete-books]]))


(def page-links 
  (for [i (range 1 2)]
    (str "http://www.goodreads.com/shelf/show/it?page=" i)))

(def extracted-book-links 
  (for [link page-links]
    (let [content (s/select (s/child (s/class "bookTitle"))
                            (as-hickory (parse (slurp link))))]
      (map #(str "http://www.goodreads.com" %)
           (map :href
                (map :attrs content))))))

(defn get-json [link]
     (slurp (str "http://www.w3.org/2012/pyMicrodata/extract?format=json&uri=" link)))

(defn prepare-json [body]
  (json/read-str
    (clojure.string/replace
      (clojure.string/replace body "@" "") "http://schema.org/" "")
    :key-fn keyword))

(defn get-book-data [json-text] 
  (second (:list (:md:item json-text))))

(defn get-review-data [json-text link] 
  (let [review-data (:reviews (first (:list (:md:item json-text))))]
    (if (nil? review-data)
      (first(remove #(= link (:id %)) (:graph json-text)))
      review-data)))

(defn get-data [link get-data-fn]
  (let [data (get-data-fn (prepare-json (get-json link)) link)] 
    (if (nil? data)
      (recur link get-data-fn)
      data)))

(defn get-user-rating [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :itemprop #(= % "ratingValue")))
                          (as-hickory (parse (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(defn extract-review [data page-link]
  (if-let [name (re-seq #"[A-Za-z]+" (apply str (second (split-at 35 (:author data)))))]
    (assoc {}
           :author (string/capitalize
                     (apply str name))
           :publishDate (:publishDate data)
           :description (:reviewBody data)
           :ratingValue (get-user-rating page-link))))

(defn prepare-review-data [links]
  (if (or (vector? links) (seq? links)) 
    (pmap #(extract-review (get-data % get-review-data) %) (flatten links))
    (extract-review (get-data links get-review-data) links)))

(defn get-image-link [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :property #(= % "og:image")))
                          (as-hickory (parse (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(defn extract-book [data page-link]
  (let [reviews (remove nil? (map #(prepare-review-data (:url %)) (:reviews data)))]
    (assoc {}
           :title (apply str (interpose " " (re-seq #"[A-Za-z0-9_]+" (:name data))))
           :image (get-image-link page-link)
           :author (:name (:author data))
           :isbn (:isbn data)
           :language (:inLanguage data)
           :bookFormatType (:bookFormatType data)
           :numberOfPages (:numberOfPages data)
           :awards (:awards data)
           :reviews reviews
           :ratingCount (count reviews)
           :ratingValue (double (/ (reduce + 0 (map #(Integer/valueOf (:ratingValue %)) reviews)) 
                                   (count reviews))))))

(defn prepare-book-data [] 
  (remove nil? (pmap #(let [data (get-book-data (prepare-json (get-json %)))]
                        (if (not (nil? data))
                          (extract-book data %))) (flatten extracted-book-links))))

(defn insert-books []
  (delete-books)
  (pmap insert-book (prepare-book-data)))

(defn process-data []
  (println "Processing data started!\n")
  (time (doall (insert-books)))
  (println (str (count (get-books)) " books imported!\n"))
  (println "Processing data finished!\n"))