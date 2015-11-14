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

;; Test 'corr-vs-imdb' function (reference value 0.569931605689576 from Excel)

(deftest example-imdb-corr
  (testing "Correlation between rates and IMDb average rates."
    (is (<
          (Math/abs ^double
                    (-
                      (corr-vs-imdb (rest (read-imdb-file "resources/example_ratings_A.csv")))
                      0.569931605689576))
          tol))))

(deftest example-imdb-count
  (testing "Number of ratings."
    (is (= (rating-num (rest (read-imdb-file "resources/example_ratings_A.csv"))) 1647))))

