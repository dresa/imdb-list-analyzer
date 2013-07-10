; Analysis tools for IMDb lists

(ns imdb-list-analyzer.analysis
  (:require
    [imdb-list-analyzer.math-tools :as mtools]
    [imdb-list-analyzer.imdb-data :as imdb]))

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

