(ns web-app.extract-data
  (:require [hickory.core :as hickory]
            [hickory.select :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clj-time.format :as time-format])
  (:use [web-app.mongo :only [get-books insert-book delete-books]]))


(def page-links
  "Prepare page links for links extraction."
  (for [i (range 1 125)]
    (str "http://www.goodreads.com/shelf/show/programming?page=" i)))

(def extracted-book-links
  "Prepare book links for data extraction."
  (atom (pmap (fn [link]
          (let [content (s/select (s/child (s/class "bookTitle"))
                                  (hickory/as-hickory
                                   (hickory/parse (slurp link))))]
            (map #(str "http://www.goodreads.com" %)
                 (map :href
                      (map :attrs content)))))
        page-links)))

(defn- get-json
  "Extract microdata from specified link in json format."
  [link]
     (slurp
      (str "http://www.w3.org/2012/pyMicrodata/extract?format=json&uri="
           link)))

(defn- prepare-json
  "Prepare json for further data extraction."
  [body]
  (json/read-str
    (string/replace
      (string/replace body "@" "") "http://schema.org/" "")
    :key-fn keyword))

(defn- get-book-data
  "Get section from json that contains book data."
  [json-text]
  (second (:list (:md:item json-text))))

(defn- get-review-data
  "Get section from json that contains review data."
  [json-text link]
  (if-let [review-data (:reviews (first (:list (:md:item json-text))))]
    review-data
    (first (remove #(= link (:id %)) (:graph json-text)))))

(defn- get-data
  "Get review data section from json if extraction fails."
  [link]
  (if-let [data (get-review-data (prepare-json (get-json link)) link)]
    data
    (recur link)))

(defn- get-user-rating
  "Extract user rating from page-link."
  [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :itemprop #(= % "ratingValue")))
                          (hickory/as-hickory (hickory/parse
                                               (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(def custom-formatter
  (time-format/formatter "MMM dd, yy"))

(defn- extract-review
  "Extract review data."
  [data page-link]
  (if-let [name (rest (clojure.string/split (:author data) #"\-"))]
    (assoc {}
           :authorId (str "gr-" (first (clojure.string/split
                         (apply str (last
                                         (clojure.string/split (:author data)
                                                               #"\/")))
                                        #"\-")))
           :author (apply str (interpose
                               " " (map clojure.string/capitalize name)))
           :publishDate (.toDate (time-format/parse custom-formatter
                                                    (:publishDate data)))
           :description (:reviewBody data)
           :ratingValue (Integer/parseInt (get-user-rating page-link)))))

(defn- prepare-review-data
  "Prepare review data for database insert."
  [links]
  (if (or (vector? links) (seq? links))
    (pmap #(extract-review (get-data %) %) (flatten links))
    (extract-review (get-data links) links)))

(defn- get-image-link
  "Extract book image from page-link."
  [page-link]
  (let [content (s/select (s/child (s/class "desktop")
                                   (s/tag :head)
                                   (s/attr :property #(= % "og:image")))
                          (hickory/as-hickory (hickory/parse
                                               (slurp page-link))))]
    (first (map :content (map :attrs content)))))

(def active-agents (atom 0))

(defn- extract-book
  "Extract book data, insert to database and decrease number of active agents."
  [page-link]
  (if-let [data (get-book-data (prepare-json (get-json page-link)))]
    (when data
      (let [reviews (remove #(or (nil? %) (zero? (:ratingValue %)))
                            (map #(prepare-review-data
                                   (:url %)) (:reviews data)))
            book (assoc {}
                        :title (string/trim (:name data))
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
                        :ratingValue (double
                                      (/ (reduce + 0
                                                 (map #(:ratingValue %)
                                                      reviews))
                                         (count reviews))))]
        (insert-book book)
        (swap! active-agents dec)))))

(defn- send-agents
  "Send agents for inserting extracted book data and increase
   number of active agents."
  []
  (dorun (map #(let [agent (agent %)]
          (send agent extract-book)
          (swap! active-agents inc))
       (first @extracted-book-links))))

(defn active-agents-watcher
  "When number of active agents is 50, remove book links for
   inserted data and start sending a new group of agents."
  [key agents old-value new-value]
  (if (= 50 new-value)
    (do
      (swap! extracted-book-links #(drop 1 %))
      (if-not (empty? @extracted-book-links)
        (send-agents)))))

(add-watch active-agents :key active-agents-watcher)

(defn process-data
  "Delete all books from database and start processing the data."
  []
  (println "Processing data started!\n")
  (delete-books)
  (send-agents))