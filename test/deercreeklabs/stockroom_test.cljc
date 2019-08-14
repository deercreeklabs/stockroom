(ns deercreeklabs.stockroom-test
  (:require
   [clojure.test :refer [deftest is]]
   [deercreeklabs.stockroom :as sr]
   [schema.core :as s]))

;; Use this instead of fixtures, which are hard to make work w/ async testing.
(s/set-fn-validation! false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Unit tests

(deftest test-stockroom
  (let [size 3
        stockroom (sr/stockroom size)]
    (is (zero?(count (sr/keys stockroom))))
    (is (nil? (sr/get stockroom 1)))
    (is (nil? (sr/get stockroom 2)))
    (is (nil? (sr/get stockroom 3)))
    (sr/put! stockroom 1 1)
    (is (= 1 (sr/get stockroom 1)))
    (is (= 1 (count (sr/keys stockroom))))
    (sr/put! stockroom 2 2)
    (is (= 2 (sr/get stockroom 2)))
    (sr/put! stockroom 3 3)
    (is (= 3 (sr/get stockroom 3)))
    (sr/put! stockroom 4 4)
    (is (= 4 (sr/get stockroom 4)))
    (is (= 3  (count (sr/keys stockroom))))
    (is (= 1 (sr/get stockroom 1)))
    (sr/put! stockroom 5 5)
    (is (= #{1 4 5} (set (sr/keys stockroom))))
    (sr/evict! stockroom 4)
    (is (= #{1 5} (set (sr/keys stockroom))))
    (sr/put! stockroom 3 3)
    (is (= #{1 3 5} (set (sr/keys stockroom))))
    (sr/flush! stockroom)
    (is (= #{} (set (sr/keys stockroom))))
    (sr/put! stockroom 5 5)
    (is (= #{5} (set (sr/keys stockroom))))
    (sr/put! stockroom 5 4)
    (is (= #{5} (set (sr/keys stockroom))))
    (is (= 4 (sr/get stockroom 5)))))

(deftest test-memoize-sr
  (let [f inc
        f* (sr/memoize-sr f 5)]
    (doseq [x (range 10)]
      (is (= [x (f x)]
             [x (f* x)])))))
