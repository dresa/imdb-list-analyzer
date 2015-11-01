;; Dummy main program: IMDb List Analyzer
;;
;; Esa Junttila 2013-06-29
;;

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.analysis :as ana]
			[imdb-list-analyzer.result-view :as resview])
  (:gen-class))

(defn -main
  "Run IMDb analysis on the example dataset."
  [& args]
  (let [titles-coll (rest (imdb/read-imdb-data "resources/example_ratings_A.csv"))]
    ;; work around dangerous default behaviour in Clojure
    (alter-var-root #'*read-eval* (constantly false))
    (do
      (println "Analyzing IMDb ratings list data...")
	  (resview/print-result (resview/compute-results titles-coll)))))
