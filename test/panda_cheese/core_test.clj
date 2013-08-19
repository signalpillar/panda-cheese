(ns panda-cheese.core-test
  (:require [clojure.test :refer :all]
            [panda-cheese.core :refer :all]))

(deftest where-condition-parsing
  (testing "simple condition-one level"
    (is (= 0 1))))
