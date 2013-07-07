;; Dummy main program: IMDb List Analyzer
;;
;; Esa Junttila 2013-06-29
;;

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb])
  (:gen-class))

(defn -main
  "Dummy program (for the time being):
   'hello world' and the number of CSV rating records."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (do
    (println "Hello, World!")
    (println (count (imdb/read-raw-data "resources/example_ratings.csv")))))
