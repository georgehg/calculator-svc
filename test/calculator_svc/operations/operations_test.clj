(ns calculator-svc.operations.operations-test
  (:require
   [calculator-svc.operations.operations :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest addition-operation-test
  (testing "Tests addition operation"
    (is (= {:result 10} (sut/calculates :addition 5 5))))

  (testing "Tests subtraction operation"
    (is (= {:result 2} (sut/calculates :subtraction 5 3))))

  (testing "Tests negative subtraction operation"
    (is (= {:result -5} (sut/calculates :subtraction 5 10))))

  (testing "Tests multiplication operation"
    (is (= {:result 25} (sut/calculates :multiplication 5 5))))

  (testing "Tests division operation"
    (is (= {:result 1} (sut/calculates :division 5 5))))

  (testing "Tests division operation by 0 "
    (is (= {:error "Divide by zero"} (sut/calculates :division 5 0))))

  (testing "Tests square-root operation"
    (is (= {:result 5.0} (sut/calculates :square-root 25))))

  (testing "Tests square-root operation with negative number"
    (is (NaN? (:error (sut/calculates :square-root -9)))))

  (testing "Tests invalid operation"
    (is (thrown? java.lang.AssertionError (sut/calculates :power 5 2)))))
