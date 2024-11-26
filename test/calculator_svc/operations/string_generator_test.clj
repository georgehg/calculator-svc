(ns calculator-svc.operations.string-generator-test
  (:require [calculator-svc.operations.string-generator :as sut]
            [clojure.test :refer [deftest is testing]]
            [shrubbery.core :refer [mock]]))

(def randm-str-mock
  (mock sut/StringGenerator
        {:random-string
         {:success "lPHaHtAgEQSLmODY7cgt"}}))

(deftest addition-operation-test
  (testing "Tests random string generation"
    (is (= {:success "lPHaHtAgEQSLmODY7cgt"} (sut/gen-string randm-str-mock)))))
