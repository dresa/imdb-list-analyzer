; Viewer for IMDb list analysis results

(ns imdb-list-analyzer.result-view
  (:require [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]))

(defrecord AnalysisResult [num mean stdev corr freq-hash entropy])

(defn compute-results
  [titles-coll]
  (map->AnalysisResult
    {:num (ana/rating-num titles-coll),
     :mean (ana/rating-mean titles-coll)
     :stdev (ana/rating-stdev titles-coll)
     :corr (ana/corr-vs-imdb titles-coll)
     :freq-hash (ana/rating-frequencies titles-coll)
     :entropy (ana/rating-entropy titles-coll)}))

(def max-entr (ana/max-entropy (count imdb/rates-range)))

(defn get-result-string
  [ana-results]
  (clojure.string/join
    ["Number of movie ratings" "\n"
     (:num ana-results) "\n"
     "Mean of movie ratings" "\n"
     (format "%.3f" (double (:mean ana-results))) "\n"
     "Standard deviation of movie ratings" "\n"
     (:stdev ana-results) "\n"
     "Correlation between ratings and IMDb rating averages" "\n"
     (:corr ana-results) "\n"
     "Frequencies of ratings" "\n"
     (clojure.string/join
       (map
         (fn [rate]
           (let [freq ((:freq-hash ana-results) rate)
                 num (:num ana-results)]
             (clojure.string/join
               ["Rate " rate " occurs " freq " times ("
                (format "%.2f" (double (* 100 (/ freq num)))) " %)\n"])))
         imdb/rates-range))
     "Entropy of ratings (in bits)" "\n"
     (format "%.3f" (:entropy ana-results)) " (maximum is " max-entr ")" "\n"
     ]))

(defn print-result
  [ana-result]
  (println (get-result-string ana-result)))

