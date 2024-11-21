(ns calculator-svc.operations-test
  (:require
   [calculator-svc.operations :as sut]
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

  (testing "Tests squareroot operation"
    (is (= {:result 5.0} (sut/calculates :squareroot 25))))

  (testing "Tests squareroot operation with negative number"
    (is (NaN? (:error (sut/calculates :squareroot -9))))))
