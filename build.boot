#!/usr/bin/env boot

(set-env! :source-paths #{"src/clj"}
          :resource-paths #{"resources"}
          :dependencies '[[org.clojure/clojure "1.7.0"]
                          [http-kit/http-kit "2.1.18"]])
(require '[org.httpkit.server :refer [run-server]]
         'conversate.core)

(defn handler
  [request]
  (prn request)
  {:status 200
   :headers {}
   :body "Hello Conversate!"})

(deftask start []
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

(task-options!
 pom {:project 'conversate
      :version "0.0.1"}
 jar {:main 'conversate.core}
 aot {:namespace '#{conversate.core}})

(deftask build []
  (comp (aot) (pom) (uber) (jar)))

(deftask run []
  (with-pass-thru _
    (conversate.core/-main)))

(defn -main [& args]
  (run))
