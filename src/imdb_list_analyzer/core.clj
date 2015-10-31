;; Dummy main program: IMDb List Analyzer
;;
;; Esa Junttila 2013-06-29
;;

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.analysis :as ana])
  (:gen-class))

(defn -main
  "Run IMDb analysis on the example dataset."
  [& args]
  (let [titles-coll (rest (imdb/read-imdb-data "resources/example_ratings.csv"))]
    ;; work around dangerous default behaviour in Clojure
    (alter-var-root #'*read-eval* (constantly false))
    (do
      (println "Analyzing IMDb ratings list data...")
      (println (count (ana/analyze titles-coll))))))



    ;(println (count (imdb/read-raw-data "resources/example_ratings.csv")))))
