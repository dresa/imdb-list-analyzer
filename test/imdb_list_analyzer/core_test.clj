;; For the time being, just a dummy testing suite for the main program.
;; Tested on Clojure 1.5.1
;;
;; Esa Junttila
;; 2013-06-30

(ns imdb-list-analyzer.core-test
  (:require [clojure.test :refer :all]
            [imdb-list-analyzer.core :refer :all]))

(deftest passing-test
  (testing "Trivial test that passes."
    (is (= 1 1))))
