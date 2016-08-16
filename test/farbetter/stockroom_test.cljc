(ns farbetter.stockroom-test
  (:refer-clojure :exclude [get])
  (:require
   #?(:cljs [cljs.test :as t])
   #?(:clj [clojure.test :refer [deftest is use-fixtures]])
   [farbetter.stockroom :as sr]
   [farbetter.utils :as u :refer
    [test-async #?@(:clj [inspect throws])]]
   [schema.core :as s :include-macros true]
   [schema.test :as st]
   [taoensso.timbre :as timbre
    #?(:clj :refer :cljs :refer-macros) [debugf errorf infof]])
  #?(:cljs
     (:require-macros
      [cljs.test :refer [deftest is use-fixtures]]
      [farbetter.utils :refer [inspect throws]])))

(use-fixtures :once schema.test/validate-schemas)

(timbre/set-level! :debug)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Unit tests

(deftest test-stockroom
  (let [size 3
        stockroom (sr/make-stockroom size)]
    (is (nil? (sr/get stockroom 1)))
    (is (nil? (sr/get stockroom 2)))
    (is (nil? (sr/get stockroom 3)))
    (sr/put stockroom 1 1)
    (is (= 1 (sr/get stockroom 1)))
    (sr/put stockroom 2 2)
    (is (= 2 (sr/get stockroom 2)))
    (sr/put stockroom 3 3)
    (is (= 3 (sr/get stockroom 3)))
    (sr/put stockroom 4 4)
    (is (= 4 (sr/get stockroom 4)))
    (is (= size (count (sr/keys stockroom))))
    (is (= 1 (sr/get stockroom 1)))
    (sr/put stockroom 5 5)
    (is (= #{1 4 5} (set (sr/keys stockroom))))))

(deftest test-set-num-keys!
  (let [stockroom (sr/make-stockroom 3)]
    (is (nil? (sr/get stockroom 1)))
    (is (nil? (sr/get stockroom 2)))
    (is (nil? (sr/get stockroom 3)))
    (sr/put stockroom 1 1)
    (is (= 1 (sr/get stockroom 1)))
    (sr/put stockroom 2 2)
    (is (= 2 (sr/get stockroom 2)))
    (sr/put stockroom 3 3)
    (is (= 3 (sr/get stockroom 3)))
    (sr/set-num-keys! stockroom 2)
    (is (= 2 (count (sr/keys stockroom))))
    (sr/put stockroom 4 4)
    (is (= 4 (sr/get stockroom 4)))
    (is (= 2 (count (sr/keys stockroom))))
    (is (= #{2 4} (set (sr/keys stockroom))))))
