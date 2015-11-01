; Viewer for IMDb list analysis results

(ns imdb-list-analyzer.result-view
  (:require [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]))

(defrecord AnalysisResult [num mean stdev corr freq-hash entropy])

(defn compute-results
  "Return a hash map of analysis results."
  [titles-coll]
  (map->AnalysisResult
    {:num (ana/rating-num titles-coll),
     :mean (ana/rating-mean titles-coll)
     :imdb-mean (ana/imdb-mean titles-coll)
     :stdev (ana/rating-stdev titles-coll)
     :imdb-stdev (ana/imdb-stdev titles-coll)
     :corr (ana/corr-vs-imdb titles-coll)
     :freq-hash (ana/rating-frequencies titles-coll)
     :imdb-freq-hash (ana/imdb-frequencies titles-coll)
     :entropy (ana/rating-entropy titles-coll)
     :imdb-entropy (ana/imdb-entropy titles-coll)
	 :dir-ranks (ana/director-qualities titles-coll)}))

(def max-entr (ana/max-entropy (count imdb/rates-range)))

(defn limited-precision
  "Return a string that represents given number 'x' with given
   number of decimals."
  [x num-decimals]
  {:pre [(number? x) (integer? num-decimals) (not (neg? num-decimals))]
   :post (string? %)}
  (let [format-str (str "%." num-decimals "f")]
    (java.lang.String/format (java.util.Locale/getDefault) format-str (to-array [(double x)]))))


(defn dir-rank-str
  [dir rank-val rates]
  (format "%-26s, %-6s, %s" (str dir) (double rank-val) (str rates)))

(defn directors-ranks-strs
  [dir-ranks title]
  (clojure.string/join
    [title "\n"
     "Director-name, Rank-p-value, Rates" "\n"
     "----------------------------------" "\n"
    (clojure.string/join "\n" (for [[[dir rates] rank-val] dir-ranks] (dir-rank-str dir rank-val rates)))
    ]))

(defn get-result-string
  "Convert analysis results into a string."
  [ana-results]
  (clojure.string/join
    ["Number of movie ratings" "\n"
     (:num ana-results) "\n"
     "Mean of movie ratings" "\n"
     (limited-precision (:mean ana-results) 3)
     " (IMDb: " (limited-precision (:imdb-mean ana-results) 3) ")" "\n"
     "Standard deviation of movie ratings" "\n"
     (limited-precision (:stdev ana-results) 3)
     " (IMDb: " (limited-precision (:imdb-stdev ana-results) 3) ")" "\n"
     "Correlation between ratings and IMDb rating averages" "\n"
     (limited-precision (:corr ana-results) 3) "\n"
     "Frequencies of ratings" "\n"
     (clojure.string/join
       (map
         (fn [rate]
           (let [freq ((:freq-hash ana-results) rate)
                 imdb-freq ((:imdb-freq-hash ana-results) rate)
                 num (:num ana-results)]
             (clojure.string/join
               ["Rate " rate " occurs " freq " times ("
                (limited-precision (* 100 (/ freq num)) 3) " %) versus IMDb " imdb-freq "\n"])))
         imdb/rates-range))
     "Entropy of ratings (in bits)" "\n"
     (limited-precision (:entropy ana-results) 3) " (maximum is " (limited-precision max-entr 3) ")"
     " (IMDb: " (limited-precision (:imdb-entropy ana-results) 3) ")" "\n"
	 "Outstanding directors:" "\n"
     (directors-ranks-strs (take 10 (:dir-ranks ana-results)) "The best directors:") "\n"
     (directors-ranks-strs (take-last 10 (:dir-ranks ana-results)) "The worst directors:") "\n"
     ]))

(defn print-result
  "Print analysis results in human-readable form to standard output."
  [ana-result]
  (println (get-result-string ana-result)))

