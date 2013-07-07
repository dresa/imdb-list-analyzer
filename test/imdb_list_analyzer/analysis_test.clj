;; Testing analytical aggregates.
;; Tested on Clojure 1.5.1
;;
;; Esa Junttila
;; 2013-07-07

(ns imdb-list-analyzer.analysis-test
  (:require [clojure.test :refer :all]
            [imdb-list-analyzer.imdb-data :refer :all]
            [imdb-list-analyzer.analysis :refer :all]))

(def tol 0.0000001)

;; Test 'corr-vs-imdb' function (reference value from Excel)

(deftest example-imdb-corr
  (testing "Correlation between rates and IMDb average rates."
    (is (< (Math/abs (- (corr-vs-imdb (rest (read-imdb-data "resources/example_ratings.csv"))) 0.597480454)) tol))))

(deftest example-imdb-count
  (testing "Number of ratings."
    (is (= (num-ratings (rest (read-imdb-data "resources/example_ratings.csv"))) 1387))))

