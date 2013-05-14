(ns web-app.extract_data
  (:require [hickory.select :as s]
            [clojure.string :as string]
            [clojure.data.json :as json])
  (:use hickory.core))


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

(defn get-author-data [json-text link] 
  (last (:list (:md:item json-text))))

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

(defn prepare-data [links get-data-fn extract-fn]
  (if (or (vector? links) (seq? links)) 
    (pmap #(extract-fn (get-data % get-data-fn)) (flatten links))
    (extract-fn (get-data links get-data-fn))))

(defn extract-author [data]
    (assoc {} 
           :name (:name data)
           :url (:url data)
           :birthDate (:birthDate data)
           :gender (:gender data)
           :review (assoc {} 
                          :ratingValue (:ratingValue (:aggregateRating data))
                          :reviewsCount (:reviewsCount (:aggregateRating data)))))

(defn extract-review [data]
  (if-let [name (re-seq #"[A-Za-z]+" (apply str (second (split-at 35 (:author data)))))]
    (assoc {}
           :author (string/capitalize
                     (apply str name))
           :url (:author data)
           :publishDate (:publishDate data)
           :description (:reviewBody data))))

(defn extract-book [data]
  (assoc {}
         :title (apply str (interpose " " (re-seq #"[A-Za-z0-9_]+" (:name data))))
         :image (:image data)
         :author (prepare-data (:url (:author data)) get-author-data extract-author)
         :isbn (:isbn data)
         :language (:inLanguage data)
         :bookFormatType (:bookFormatType data)
         :numberOfPages (:numberOfPages data)
         :awards (:awards data)
         :reviews (remove nil? 
                          (map #(prepare-data (:url %) get-review-data extract-review) 
                               (:reviews data)))))

(defn prepare-book-data [links]
  (remove nil? (pmap #(let [data (get-book-data (prepare-json (get-json %)))]
                        (if (not (nil? data))
                          (extract-book data))) (flatten links))))

(defn process-data []
  (println "Processing data started!\n")
  (prepare-book-data extracted-book-links)
  (println "Processing data finished!\n"))