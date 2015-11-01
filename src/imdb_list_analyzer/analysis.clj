; Analysis tools for IMDb lists

(ns imdb-list-analyzer.analysis
  (:require
    [imdb-list-analyzer.math-tools :as mtools]
    [imdb-list-analyzer.imdb-data :as imdb]
    [imdb-list-analyzer.common :as com]))

(defn corr-vs-imdb
  [titles-coll]
  (mtools/correlation (map :rate titles-coll) (map :imdb-rate titles-coll)))

(defn rating-num
  [titles-coll]
  (count titles-coll))

(defn rating-mean
  [titles-coll]
  (mtools/mean (map :rate titles-coll)))

(defn imdb-mean
  [titles-coll]
  (mtools/mean (map :imdb-rate titles-coll)))

(defn rating-stdev
  [titles-coll]
  (mtools/stdev (map :rate titles-coll)))

(defn imdb-stdev
  [titles-coll]
  (mtools/stdev (map :imdb-rate titles-coll)))

(defn rating-entropy
  [titles-coll]
  (mtools/entropy (vals (frequencies (map :rate titles-coll)))))

(defn imdb-entropy
  [titles-coll]
  (mtools/entropy (vals (frequencies (map #(Math/round (:imdb-rate %)) titles-coll)))))

(defn max-entropy
  [n]
  (mtools/entropy (repeat n 1)))

(defn rating-frequencies
  [titles-coll]
  (merge
    (zipmap imdb/rates-range (repeat (count imdb/rates-range) 0))  ; defaults
    (frequencies (map :rate titles-coll))))  ; actual nonzero frequencies

(defn imdb-frequencies
  [titles-coll]
  (merge
    (zipmap imdb/rates-range (repeat (count imdb/rates-range) 0))  ; defaults
    (frequencies (map #(Math/round (:imdb-rate %)) titles-coll))))  ; actual nonzero frequencies

(defn rating-directors
  [titles-coll]
  (com/invert-multimap
    (map #(vector (:rate %) (:directors %)) titles-coll)))

(defn sample-null-ref-value
  [num-values emp-distr]
  (mtools/mean (take num-values (mtools/sample-distr emp-distr))))

(def num-samples 1000)

(defn compute-reference-value
  [rates emp-distr distr-mu]
  (let [mu (mtools/mean rates)
        num (count rates)
        samples (take num-samples (repeatedly #(sample-null-ref-value num emp-distr)))]
    (/ (count (filter #(if (<= distr-mu mu) (< % mu) (<= % mu)) samples)) (count samples))))

(defn director-empirical-rank
  [director-rate-lists emp-distr]
  (let [distr-mu (mtools/mean (map first (:symbcumuprobs emp-distr)))]
    (map #(compute-reference-value % emp-distr distr-mu) director-rate-lists)))

(defn director-rank
  [titles-coll]
  (let [dirs (rating-directors titles-coll)
        emp-distr (mtools/gen-emp-distr (map :rate titles-coll))]
    (map vector dirs (director-empirical-rank (map second dirs) emp-distr))))

(defn director-qualities
  [titles-coll]
  (reverse (sort-by second (director-rank titles-coll))))

