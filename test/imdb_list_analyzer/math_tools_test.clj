;; Testing mathematical and analytical functions.
;; Tested on Clojure 1.5.1
;;
;; Esa Junttila
;; 2013-06-30

(ns imdb-list-analyzer.math-tools-test
  (:require [clojure.test :refer :all]
            [imdb-list-analyzer.math-tools :refer :all]))

(def tol 0.0000001)


;; Test 'sum' function

(deftest sum-empty
  (testing "Sum of an empty list."
    (is (= (sum []) 0))))

(deftest sum-size-one
  (testing "Sum of a list of size one."
    (is (= (sum [-7]) -7))))

(deftest sum-several-numbers
  (testing "Sum of a list of several numbers."
    (is (= (sum [2 3 4]) 9))))

(deftest sum-nan
  (testing "Sum of a list that contains NaN."
    (is (Double/isNaN (sum [2 Double/NaN 4])))))

(deftest sum-pos-neg
  (testing "Sum of a list that contains both positive and negative numbers."
    (is (= (sum [-2 3 -4]) -3))))

; Computing fractions: 4/3 - 5/3 + 2/3 = (4 - 5 + 2)/3 = 1 / 3
(deftest sum-fractions
  (testing "Sum of a list of fractions."
    (is (= (sum [(/ 4 3) (- (/ 5 3)) (/ 2 3)]) (/ 3)))))


;; Test 'mean' function

(deftest mean-empty
  (testing "Mean of an empty list."
    (is (Double/isNaN (mean [])))))

(deftest mean-nan
  (testing "Mean of a list that contains NaN."
    (is (Double/isNaN (mean [1 Double/NaN 3])))))

(deftest mean-one
  (testing "Mean of a list of size one."
    (is (= (mean [-7]) -7))))

(deftest mean-ints
  (testing "Mean of a list of integers."
    (is (= (mean [1 2 3 4 5]) 3))))

(deftest mean-pos-neg
  (testing "Mean of a list of both positive and negative integers."
    (is (= (mean [-5 6 7 -8]) 0))))

(deftest mean-fraction
  (testing "Fraction mean of a list of integers."
    (is (= (mean [100 101 110 119]) (/ 430 4)))))


;; Test 'dot-product' function
;(dot-product [2 3 -4] [-5 6 7 0])  ;throws AssertionError

(deftest dot-product-empty
  (testing "Dot product of empty lists."
    (is (= (dot-product [] []) 0))))

(deftest dot-product-one
  (testing "Dot product of vectors of size one."
    (is (= (dot-product [0] [2]) 0))))

(deftest dot-product-nan
  (testing "Dot product of a lists one of which contains NaN."
    (is (Double/isNaN (dot-product [1 Double/NaN 3] [5 6 7])))))

(deftest dot-product-pos-neg
  (testing "Dot product of lists with both positive and negative integers."
    (is (= (dot-product [2 3 -4] [-5 6 7]) -20))))


;; Test 'variance' function (reference values computed in R)
;(variance [7])  ;throws AssertionError
;(variance [])   ;throws AssertionError

(deftest variance-int
  (testing "Variance of a list of positive integers."
    (is (= (variance [1 2]) (/ 2)))))

(deftest variance-pos-neg
  (testing "(Double) variance of a list of both positive and negative integers."
    (is (= (double (variance [1 -10 13 -15 16])) 186.5))))

(deftest variance-nan
  (testing "Variance of a list that contains NaN."
    (is (Double/isNaN (variance [1 Double/NaN 3])))))

(deftest variance-one-unique
  (testing "Variance of a list with only one unique number."
    (is (= (variance [7 7 7 7 7 7]) 0))))



;; Test 'stdev' function (reference values computed in R)
;(stdev [1])  ;throws AssertionError

(deftest stdev-pos-int
  (testing "Standard deviation of a list of positive integers."
    (is (= (stdev [1 2]) (Math/sqrt (/ 2))))))

(deftest stdev-int-tol
  (testing "Standard deviation of a list of integers is within a tolerance."
    (is (< (Math/abs (- (stdev [1 2 -7 19 50]) 22.74863)) 0.00001))))

(deftest stdev-nan
  (testing "Standard deviation of a list that contains NaN."
    (is (Double/isNaN (stdev [-1 Double/NaN -3])))))

(deftest stdev-one-unique-frac
  (testing "Standard deviation of a list with just one unique fraction."
    (is (= (stdev [(/ 9) (/ 9) (/ 9) (/ 9) (/ 9)]) 0.0))))


;; Test 'correlation' function (reference values computed in R)
;(correlation [] [])  ;throws AssertionError
;(correlation [1] [])  ;throws AssertionError
;(correlation [1] [4])  ;throws AssertionError
;(correlation [] [6])  ;throws AssertionError
;(correlation [1 4] [6])  ;throws AssertionError
;(correlation [4] [6 6])  ;throws AssertionError
;(correlation [1 2 3] [9 8 7 6])  ;throws AssertionError

(deftest correlation-pos-int
  (testing "Pearson correlation of lists of positive integers."
    (is (< (Math/abs (- (correlation [1 2] [2 9]) 1.0)) tol))))

(deftest correlation-pos-neg
  (testing "Linear Pearson correlation of both positive and negative integers."
    (is (< (Math/abs (- (correlation [-1 0 1 2 5] [1 2 3 4 7]) 1.0)) tol))))

(deftest correlation-int-double-1
  (testing "Pearson correlation of lists of integers, within a tolerance."
    (is (< (Math/abs (- (correlation [1 2 3] [2 3 5]) 0.9819805)) tol))))

(deftest correlation-int-double-2
  (testing "Pearson correlation of lists of positive and negative integers, within a tolerance."
    (is (< (Math/abs (- (correlation [1 -2 4 6 3] [3 3 5 2 -4]) -0.1102462)) tol))))
