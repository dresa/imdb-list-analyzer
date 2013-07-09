; Viewer for IMDb list analysis results

(ns imdb-list-analyzer.result-view
  (:require [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]))

(defrecord AnalysisResult [num-rates imdb-corr rate-freq-hash])

(defn compute-results
  [titles-coll]
  (map->AnalysisResult
    {:num-rates (ana/rating-num titles-coll),
     :imdb-corr (ana/corr-vs-imdb titles-coll)
     :rate-freq-hash (ana/rating-frequencies titles-coll)}))

(defn get-result-string
  [ana-results]
  (clojure.string/join
    ["Number of movie ratings" "\n"
     (:num-rates ana-results) "\n"
     "\n"
     "Frequencies of ratings" "\n"
     (clojure.string/join
       (map
         (fn [rate]
           (let [freq ((:rate-freq-hash ana-results) rate)
                 num (:num-rates ana-results)]
             (clojure.string/join
               ["Rate " rate " occurs " freq " times ("
                (format "%.2f" (double (* 100 (/ freq num)))) " %)\n"])))
         imdb/rates-range))
     "\n"
     "Correlation between ratings and IMDb rating averages" "\n"
     (:imdb-corr ana-results) "\n"
     ]))

(defn print-result
  [ana-result]
  (println (get-result-string ana-result)))

