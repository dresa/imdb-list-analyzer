;; Testing mathematical and analytical functions.
;; Tested on Clojure 1.5.1
;;
;; Esa Junttila
;; 2013-07-02

(ns imdb-list-analyzer.math-tools-test
  (:require [clojure.test :refer :all]
            [imdb-list-analyzer.math-tools :refer :all]))

(def tol 0.0000001)


;; Test 'sum' function

(deftest sum-noncoll
  (testing "Sum of a non-collections and nils."
    (is (thrown? AssertionError (sum 7)))
    (is (thrown? AssertionError (sum nil)))
    (is (thrown? AssertionError (sum [1 [3] 43])))
    (is (thrown? AssertionError (sum [1 43 nil])))))

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

(deftest mean-noncoll
  (testing "Mean of a non-collections and nils."
    (is (thrown? AssertionError (mean 7)))
    (is (thrown? AssertionError (mean nil)))
    (is (thrown? AssertionError (mean [1 [4] -7])))
    (is (thrown? AssertionError (mean [1 nil -7])))))

(deftest mean-empty
  (testing "Mean of an empty list."
    (is (Double/isNaN (mean [])))))

(deftest mean-nan
  (testing "Mean of a list that contains NaN."
    (is (Double/isNaN (mean [1 Double/NaN 3])))))

(deftest mean-one
  (testing "Mean of a list of size one."
    (is (= (mean '(-7)) -7))))

(deftest mean-ints
  (testing "Mean of a list of integers."
    (is (= (mean [1 2 3 4 5]) 3))))

(deftest mean-pos-neg
  (testing "Mean of a list of both positive and negative integers."
    (is (zero? (mean [-5 6 7 -8])))))

(deftest mean-fraction
  (testing "Fraction mean of a list of integers."
    (is (= (mean [100 101 110 119]) (/ 430 4)))))


;; Test 'dot-product' function

(deftest dot-product-type
  (testing "Dot-product of a non-collections and nils."
    (is (thrown? AssertionError (dot-product 7 [5])))
    (is (thrown? AssertionError (dot-product [5] nil)))
    (is (thrown? AssertionError (dot-product [5 5 6] [-2 [] -4])))
    (is (thrown? AssertionError (dot-product [5 5 6] [-2 nil -4])))))

(deftest dot-product-mismatch
  (testing "Dot product with differing argument sizes"
    (is (thrown? AssertionError (dot-product [] [2])))
    (is (thrown? AssertionError (dot-product [2 3 -4] [-5 6 7 0])))))

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

(deftest variance-arg
  (testing "AssertionError on too small argument length."
    (is (thrown? AssertionError (variance 6)) "not a collection")
    (is (thrown? AssertionError (variance nil)) "not a collection")
    (is (thrown? AssertionError (variance Double/NaN)) "not a collection")
    (is (thrown? AssertionError (variance [])) "too small collection")
    (is (thrown? AssertionError (variance [7])) "too small collection")
    (is (thrown? AssertionError (variance [1 2 nil])) "non-number in collection")
    (is (thrown? AssertionError (variance [1 [] 2])) "non-number in collection")
    ))
    

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

(deftest stdev-mismatch
  (testing "Standard deviation with too small coll size"
    (is (thrown? AssertionError (stdev [])))
    (is (thrown? AssertionError (stdev [1])))))

(deftest stdev-pos-int
  (testing "Standard deviation of a list of positive integers."
    (is (= (stdev [1 2]) (Math/sqrt (/ 2))))))

(deftest stdev-int-tol
  (testing "Standard deviation of a list of integers is within a tolerance."
    (is (< (Math/abs ^double (- (stdev [1 2 -7 19 50]) 22.74863)) 0.00001))))

(deftest stdev-nan
  (testing "Standard deviation of a list that contains NaN."
    (is (Double/isNaN (stdev [-1 Double/NaN -3])))))

(deftest stdev-one-unique-frac
  (testing "Standard deviation of a list with just one unique fraction."
    (is (= (stdev [(/ 9) (/ 9) (/ 9) (/ 9) (/ 9)]) 0.0))))


;; Test 'correlation' function (reference values computed in R)

(deftest correlation-mismatch
  (testing "Pearson correlation with differing argument sizes"
    (is (thrown? AssertionError (correlation [3 4] nil)))
    (is (thrown? AssertionError (correlation Double/NaN [7 8])))
    (is (thrown? AssertionError (correlation [4 5 [6]] [7 8 9])))
    (is (thrown? AssertionError (correlation [1 2 3] [4 nil 6])))
    (is (thrown? AssertionError (correlation [1] [])))
    (is (thrown? AssertionError (correlation [1] [4])))
    (is (thrown? AssertionError (correlation [] [6])))
    (is (thrown? AssertionError (correlation [1 4] [6])))
    (is (thrown? AssertionError (correlation [4] [6 6])))
    (is (thrown? AssertionError (correlation [3 6] [4 nil])))
    (is (thrown? AssertionError (correlation [4 7] [6 [6]])))
    (is (thrown? AssertionError (correlation [1 2 3] [9 8 7 6])))))

(deftest correlation-pos-int
  (testing "Pearson correlation of lists of positive integers."
    (is (< (Math/abs (- (correlation [1 2] [2 9]) 1.0)) tol))))

(deftest correlation-pos-neg
  (testing "Linear Pearson correlation of both positive and negative integers."
    (is (< (Math/abs (- (correlation [-1 0 1 2 5] [1 2 3 4 7]) 1.0)) tol))))

(deftest correlation-zero-variance
  (testing "Linear Pearson correlation of a zero-variance collection."
    (is (Double/isNaN (correlation [-1 0 1 2 5] [3 3 3 3 3])))))

(deftest correlation-int-double-1
  (testing "Pearson correlation of lists of integers, within a tolerance."
    (is (< (Math/abs (- (correlation [1 2 3] [2 3 5]) 0.9819805)) tol))))

(deftest correlation-int-double-2
  (testing "Pearson correlation of lists of positive and negative integers, within a tolerance."
    (is (< (Math/abs (- (correlation [1 -2 4 6 3] [3 3 5 2 -4]) -0.1102462)) tol))))

(deftest correlation-int-double-3
  (testing "Pearson correlation of large integers, within a tolerance."
    (is (< (Math/abs (- (correlation [1000000000 -2000000000 1000000000] [500000000 1500000000 -400000000]) -0.8808123)) tol))))

(deftest linear-interp-in-1
  (testing "Linear interpolation within bounds."
    (is (= (linear-interp 2 100 12 150 8) 130))))

(deftest linear-interp-in-2
  (testing "Linear interpolation within bounds, with negatives."
    (is (= (linear-interp -2 100 12 150 8) (+ 100 (/ 250 7))))))

(deftest linear-interp-in-3
  (testing "Linear interpolation at the low extreme."
    (is (= (linear-interp 2 100 12 150 2) 100))))

(deftest linear-interp-in-4
  (testing "Linear interpolation at the high extreme."
    (is (= (linear-interp 2 100 12 150 12) 150))))

(deftest linear-interp-out-1
  (testing "Flat extrapolation at the low end."
    (is (= (linear-interp 2 100 12 150 -4 :flat) 100))))

(deftest linear-interp-out-2
  (testing "Flat extrapolation at the high end."
    (is (= (linear-interp 2 100 12 150 16 :flat) 150))))

(deftest linear-interp-out-3
  (testing "Linear extrapolation at the low end."
    (is (= (linear-interp 2 100 12 150 -4 :linear) 70))))

(deftest linear-interp-out-4
  (testing "Linear extrapolation at the high end."
    (is (= (linear-interp 2 100 12 150 16 :linear) 170))))

(deftest ecdf-1
  (testing "Strict empirical cumulative probability, basic calls."
    (is (= (ecdf 8 [5 6 7 8 9] [30 15 20 25 10]) (/ 90 100)))
    (is (= (ecdf 5 [5 6 7 8 9] [30 15 20 25 10])) (/ 30 100))
    (is (zero? (ecdf 4.9 [5 6 7 8 9] [30 15 20 25 10])))
    (is (zero? (ecdf -9 [5 6 7 8 9] [30 15 20 25 10])))
    (is (= (ecdf 9 [5 6 7 8 9] [30 15 20 25 10]) 1))
    (is (= (ecdf 123 [5 6 7 8 9] [30 15 20 25 10]) 1))))


(deftest old-smooth-ecdf-1
  (testing "Smooth empirical cumulative probability, at class bound."
    (is (= (old-smooth-ecdf (/ 17 2) [5 6 7 8 9] [30 15 20 25 10]) (/ 90 100)))))

(deftest old-smooth-ecdf-2
  (testing "Smooth empirical cumulative probability, within a class."
    (is (= (old-smooth-ecdf 8 [5 6 7 8 9] [30 15 20 25 10]) (/ (+ 65 (/ 25 2)) 100)))))

(deftest smooth-ecdf-1
  (testing "Smooth empirical cumulative probability, at class bound."
    (is (= (smooth-ecdf (/ 17 2) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 90 100)))))

(deftest smooth-ecdf-2
  (testing "Smooth empirical cumulative probability, within a class."
    (is (= (smooth-ecdf 8 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 775 1000)))))

(deftest smooth-ecdf-3
  (testing "Smooth empirical cumulative probability, within start class."
    (is (= (smooth-ecdf 5 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 15 100)))))

(deftest smooth-ecdf-4
  (testing "Smooth empirical cumulative probability, within a truncated start class."
    (is (zero? (smooth-ecdf 5 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true)))))

(deftest smooth-ecdf-5
  (testing "Smooth empirical cumulative probability, at left-border of a start class."
    (is (zero? (smooth-ecdf (/ 9 2) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}))))))

(deftest smooth-ecdf-6
  (testing "Smooth empirical cumulative probability, before a truncated start class."
    (is (zero? (smooth-ecdf (/ 9 2) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true)))))

(deftest smooth-ecdf-7
  (testing "Smooth empirical cumulative probability, at left from a start class."
    (is (zero? (smooth-ecdf -4 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}))))))

(deftest smooth-ecdf-8
  (testing "Smooth empirical cumulative probability, left from a truncated start class."
    (is (zero? (smooth-ecdf -4 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true)))))

(deftest smooth-ecdf-9
  (testing "Smooth empirical cumulative probability, within end class."
    (is (= (smooth-ecdf 9 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 95 100)))))

(deftest smooth-ecdf-10
  (testing "Smooth empirical cumulative probability, within a truncated end class."
    (is (= (smooth-ecdf 9 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true) 1))))

(deftest smooth-ecdf-11
  (testing "Smooth empirical cumulative probability, at right-border of a end class."
    (is (= (smooth-ecdf (/ 95 10) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) 1))))

(deftest smooth-ecdf-12
  (testing "Smooth empirical cumulative probability, after a truncated end class."
    (is (= (smooth-ecdf (/ 95 10) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true) 1))))

(deftest smooth-ecdf-13
  (testing "Smooth empirical cumulative probability, at right from a end class."
    (is (= (smooth-ecdf 999 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) 1))))

(deftest smooth-ecdf-14
  (testing "Smooth empirical cumulative probability, right from a truncated end class."
    (is (= (smooth-ecdf 999 (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true) 1))))

(deftest smooth-ecdf-15
  (testing "Smooth empirical cumulative probability, within a class."
    (is (= (smooth-ecdf (/ 675 100) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 5 10)))))

(deftest smooth-ecdf-16
  (testing "Smooth empirical cumulative probability, within a class (truncated ends)."
    (is (= (smooth-ecdf (/ 675 100) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true) (/ 5 10)))))

(deftest smooth-ecdf-17
  (testing "Smooth empirical cumulative probability, within first class."
    (is (= (smooth-ecdf (/ 51 10) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10})) (/ 18 100)))))

(deftest smooth-ecdf-18
  (testing "Smooth empirical cumulative probability, within first class (truncated ends)."
    (is (= (smooth-ecdf (/ 51 10) (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true) (/ 6 100)))))

(deftest smooth-ecdf-18
  (testing "Smooth empirical cumulative probability, selection of points."
    (is (= (map
             #(smooth-ecdf % (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}))
             [-2 0 4 (/ 45 10) (/ 49 10) 5 (/ 52 10) (/ 54 10) (/ 55 10) (/ 56 10) 6 (/ 64 10) (/ 66 10) (/ 69 10) 7 (/ 77 10) 8 (/ 81 10) (/ 849 100) (/ 851 100) (/ 89 10) 9 (/ 94 10) (/ 95 10) (/ 96 10) 777])
           [0 0 0 0N 3/25 3/20 21/100 27/100 3/10 63/200 3/8 87/200 47/100 53/100 11/20 7/10 31/40 4/5 359/400 901/1000 47/50 19/20 99/100 1N 1N 1N]))))

(deftest smooth-ecdf-19
  (testing "Smooth empirical cumulative probability, selection of points, with truncated ends."
    (is (= (map
             #(smooth-ecdf % (generate-emp-distr {5 30, 6 15, 7 20, 8 25, 9 10}) true)
             [-2 0 4 (/ 45 10) (/ 49 10) 5 (/ 52 10) (/ 54 10) (/ 55 10) (/ 56 10) 6 (/ 64 10) (/ 66 10) (/ 69 10) 7 (/ 77 10) 8 (/ 81 10) (/ 849 100) (/ 851 100) (/ 89 10) 9 (/ 94 10) (/ 95 10) (/ 96 10) 777])
           [0 0 0 0 0 0N 3/25 6/25 3/10 63/200 3/8 87/200 47/100 53/100 11/20 7/10 31/40 4/5 359/400 451/500 49/50 1N 1N 1N 1N 1N]))))
