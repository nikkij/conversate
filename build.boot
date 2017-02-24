#!/usr/bin/env boot

(set-env! :source-paths #{"src/clj"}
          :resource-paths #{"resources"}
          :dependencies '[[org.clojure/clojure "1.7.0"]
                          [http-kit/http-kit "2.1.18"]])

(require '[org.httpkit.server :refer [run-server]]
         'conversate.core
         'conversate.scraper)

(task-options!
 pom {:project 'conversate
      :version "0.0.1"}
 jar {:main 'conversate.core}
 aot {:namespace '#{conversate.core}})

;; Trying to separate the app into a bunch of different areas
;; Server - Webscraper - NLP Processor
;; and create a seperate boot task for each of them
;; Not sure how thats supposed to work in production...

;; http-kit server and handler
(defn handler
  [request]
  (prn request)
  {:status 200
   :headers {}
   :body "Hello Conversate!"})

(deftask start-server []
  (let [shutdown (promise)
         stop-server (run-server handler {:port 3000})]
    (do
      (.addShutdownHook (.. Runtime getRuntime)
                        (Thread. (fn []
                                   (do
                                     (println "shutting down...")
                                     (@stop-server)
                                     (deliver shutdown nil)))))
      (println "listening on 3000")
      @shutdown)))

;; Webscraper
(deftask run-scraper []
  (with-pass-thru _
    (conversate.scraper/start)))

;; unused bullshit below here?
(deftask build []
  (comp (aot) (pom) (uber) (jar)))

(deftask run []
  (with-pass-thru _
    (conversate.core/-main)))

