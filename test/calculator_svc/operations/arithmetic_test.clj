(ns calculator-svc.operations.arithmetic-test
  (:require
   [calculator-svc.operations.arithmetic :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest addition-operation-test
  (testing "Tests addition operation"
    (is (= {:success 10} (sut/calculates :addition 5 5))))

  (testing "Tests subtraction operation"
    (is (= {:success 2} (sut/calculates :subtraction 5 3))))

  (testing "Tests negative subtraction operation"
    (is (= {:success -5} (sut/calculates :subtraction 5 10))))

  (testing "Tests multiplication operation"
    (is (= {:success 25} (sut/calculates :multiplication 5 5))))

  (testing "Tests division operation"
    (is (= {:success 1} (sut/calculates :division 5 5))))

  (testing "Tests division operation by 0 "
    (is (= {:error "Divide by zero"} (sut/calculates :division 5 0))))

  (testing "Tests square-root operation"
    (is (= {:success 5.0} (sut/calculates :square-root 25))))

  (testing "Tests square-root operation with negative number"
    (is (NaN? (:error (sut/calculates :square-root -9)))))

  (testing "Tests invalid operation"
    (is (thrown? java.lang.AssertionError (sut/calculates :power 5 2)))))
