# web-app

A Clojure web application for listing programming books, ratings, reviews and recommendations.  
The application is written in Clojure and uses the following libraries:  
[Ring](https://github.com/ring-clojure/ring),  
[Compojure](https://github.com/weavejester/compojure),  
[Hiccup](https://github.com/weavejester/hiccup), 
[CongoMongo](https://github.com/aboekhoff/congomongo),  
[Lib-noir](https://github.com/noir-clojure/lib-noir), 
[Mongo-session](https://github.com/amalloy/mongo-session), 
[Hickory](https://github.com/davidsantiago/hickory), 
[Data.json](https://github.com/clojure/data.json) and 
[Clj-time](https://github.com/clj-time/clj-time).
  
The application is designed for learning Clojure.

When web application is started, initial user list is imported and data extraction is  
started. Data are exported from [Goodreads](http://www.goodreads.com/). Some data are 
extracted using [Microdata to RDF Distiller](http://www.w3.org/2012/pyMicrodata/) and some  
directly from web pages. All books related to programming are being crawled.

Number of books and reviews imported is shown on the Home page.

User can register and log in. After logging in, user is redirected to the Home page. 

To be able to check registered user list or to delete an existing user, log in as admin. 
Admin credentials are: 
 - username: admin
 - password admin

All imported books are listed on Books page. User can search for a book using book 
title, author or ISBN. 

Book data, ratings and reviews are shown at Book page. Only if user is logged in, he 
can rate a book and add a comment. Also, all similar books according to user ratings are 
listed. Similarities are calculated using the Pearson correlation score. 
	
## Setup instructions for running locally

* Download and install [Leiningen](https://github.com/technomancy/leiningen).

* Download and install [MongoDB](http://www.mongodb.org/). 

* Start MongoDB.

* To run the tests, cd to the web-app project directory and run `lein test`.

* To start the application, cd to the web-app project directory and run `lein run`.
(If you use leiningen 1, run `lein deps` first.)

Note: If you are starting the application for the first time, note that importing 
of books and reviews will start in a few minutes. 
If you have already imported desired number of books, before starting the application  
for the second time, in main function, in core.clj file, put (process-data) line 
under comments, otherwise all previously imported books will be deleted and import   
will start from the beginning.

## References

* [Practical Clojure](http://www.amazon.com/Practical-Clojure-Experts-Voice-Source/dp/1430272317), Luke VanderHart and Stuart Sierra, 
[Clojure Programming](http://www.amazon.com/Clojure-Programming-Chas-Emerick/dp/1449394701), Chas Emerick, Brian Carper and Chrisophe Grand and 
[4clojure](http://www.4clojure.com/) - for learning Clojure
  
* [Developing and Deploying a Simple Clojure Web Application](http://mmcgrana.github.io/2010/07/develop-deploy-clojure-web-applications.html) and 
[A brief overview of the Clojure web stack](http://brehaut.net/blog/2011/ring_introduction) 
for learning 
[Ring](https://github.com/ring-clojure/ring), 
[Compojure](https://github.com/weavejester/compojure) and 
[Hiccup](https://github.com/weavejester/hiccup)

* [Programming Collective Intelligence](http://www.amazon.com/Programming-Collective-Intelligence-Building-Applications/dp/0596529325), Toby Seagaran 
- book recommendations are created based on algorithms in chapter 2. - Making Recommendations 
- recommendations.clj
  
* [CongoMongo](https://github.com/aboekhoff/congomongo) library for using MongoDB with Clojure 
- mongo.clj

* [Hickory](https://github.com/davidsantiago/hickory) library for parsing HTML used to extract data from web page 
- extract_data.clj.

## License

Distributed under the Eclipse Public License, the same as Clojure.
