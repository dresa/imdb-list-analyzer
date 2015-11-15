;;; Viewer for results from IMDb list analysis (on a single list)

(ns imdb-list-analyzer.result-view
  (:require [clojure.string :as string]
            [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]
            [cheshire.core :as json])
  (:import (java.util Locale)))

"Collection of all analysis results"
(defrecord AnalysisResult [num mean stdev corr freq-hash entropy]) ; TODO: should we list all keys?

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

"Maximum Shannon information entropy, given the rating scale"
(def max-entr (ana/max-entropy (count imdb/rates-range)))

(defn limited-precision
  "Return a string that represents given number 'x' with given
   number of decimals."
  [x num-decimals]
  {:pre [(number? x) (integer? num-decimals) (not (neg? num-decimals))]
   :post (string? %)}
  (let [format-str (str "%." num-decimals "f")]
    (String/format (Locale/getDefault) format-str (to-array [(double x)]))))

(defn dir-rank-str
  "String representation of a single director result, along with p-value and ratings"
  [dir rank-val rates]
  (format "%-26s; %-6s; %s" (str dir) (double rank-val) (str rates)))

(defn directors-ranks-strs
  "String representation of a collection of director results,
  along with p-values and individual ratings"
  [dir-ranks title]
  (string/join
    [title "\n"
     "Director-name; Rank-p-value; Rates" "\n"
     "----------------------------------" "\n"
    (string/join "\n" (for [[[dir rates] rank-val] dir-ranks] (dir-rank-str dir rank-val rates)))]))

(defn view-count-str
  "String representation for title count"
  [ana-results]
  (string/join [
    "Number of movie ratings" "\n"
    (:num ana-results) "\n"]))

(defn view-mean-str
  "String representation for mean rating"
  [ana-results]
  (string/join [
    "Mean of movie ratings" "\n"
    (limited-precision (:mean ana-results) 3)
    " (IMDb: " (limited-precision (:imdb-mean ana-results) 3) ")\n"]))

(defn view-sd-str
  "String representation for standard deviation of ratings"
  [ana-results]
  (string/join [
     "Standard deviation of movie ratings" "\n"
     (limited-precision (:stdev ana-results) 3)
     " (IMDb: " (limited-precision (:imdb-stdev ana-results) 3) ")\n"]))

(defn view-corr-str
  "String representation for ratings correlation"
  [ana-results]
  (string/join [
     "Correlation between ratings and IMDb rating averages" "\n"
     (limited-precision (:corr ana-results) 3) "\n"]))

(defn view-freq-str
  "String representation for ratings frequencies (vs. IMDb rounded averages)"
  [ana-results]
  (string/join [
     "Frequencies of ratings" "\n"
     (string/join
       (map
         (fn [rate]
           (let [freq ((:freq-hash ana-results) rate)
                 imdb-freq ((:imdb-freq-hash ana-results) rate)
                 num (:num ana-results)]
             (string/join
               ["Rate " rate " occurs " freq " times ("
                (limited-precision (* 100 (/ freq num)) 3) " %) versus IMDb " imdb-freq "\n"])))
         imdb/rates-range))]))

(defn view-entropy-str
  "String representation for rating distribution entropy"
  [ana-results]
  (let [entr (limited-precision (:entropy ana-results) 3)
        imdb-entr (limited-precision (:imdb-entropy ana-results) 3)
		max-entr (limited-precision max-entr 3)]
    (string/join [
      "Entropy of ratings (in bits)" "\n"
      entr " (in IMDb " imdb-entr "; maximum is " max-entr ")\n"])))

(defn view-directors-str
  "String representation for the best and worst directors"
  [ana-results]
  (string/join [
    (directors-ranks-strs (take 10 (:dir-ranks ana-results)) "The best directors:") "\n\n"
    (directors-ranks-strs (take-last 10 (:dir-ranks ana-results)) "The worst directors:") "\n"]))

(defn view-results-str
  "String representation of all analysis results"
  [ana-results]
  (string/join "\n"
    ["-------------------------------------"
     "- IMDb single-list analysis results -"
     "-------------------------------------"
     (view-count-str ana-results)
     (view-mean-str ana-results)
     (view-sd-str ana-results)
     (view-corr-str ana-results)
     (view-freq-str ana-results)
     (view-entropy-str ana-results)
     (view-directors-str ana-results)]))

(defn view-results
  "Print all single-list analysis results in human-readable form to standard output."
  [ana-result]
  (println (view-results-str ana-result)))

(defn jsonify-single-result
  "JSON string of an AnalysisResult."
  [single-results]
  (json/generate-string {"singleresults" single-results}))
