(ns calculator-svc.operations.service-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [sut]}}}}
  (:require
   [calculator-svc.containers.mysql-container :as container]
   [calculator-svc.helpers :refer [with-system]]
   [calculator-svc.infra.mysql-adapter :refer [new-mysql]]
   [calculator-svc.operations.repository :refer [get-operations-records]]
   [calculator-svc.operations.service :as svc]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [com.stuartsierra.component :as component]))

(defn- service-system []
  (let [config container/config+container]
    (component/system-map
     :component/db.mysql (new-mysql {:mysql (:mysql config)
                                     :migration (:migration config)})
     :component/service (component/using (svc/new-calculator-service)
                                         {:db :component/db.mysql}))))

(use-fixtures :each container/mysql-fixtures)

(deftest tests-success-calculation-with-cost
  (with-system [sut (service-system)]
    (testing "Should return calculation result with user balance withdrawned and operation recorded"
      (let [result (svc/calculate-with-costs (:component/service sut)
                                             1
                                             :operations/addition
                                             [5 10])]
        (is (= {:response-type :success
                :result 15
                :cost 0.50M
                :user-balance 4.50M}
               result))

        (is (= {:id 1
                :username "john.doe@example.com"
                :type "addition"
                :amount 0.50M
                :user_balance 4.50M
                :operation_result "{:success 15}"}
               (-> (get-operations-records (:component/db.mysql sut) 1 0 10)
                   first
                   (dissoc :created_at))))))

    (testing "Should return calculation result with error once user is out of balance"

      (svc/calculate-with-costs (:component/service sut) 1 :operations/division [100 10])
      (svc/calculate-with-costs (:component/service sut) 1 :operations/division [100 10])
      (svc/calculate-with-costs (:component/service sut) 1 :operations/division [100 10])
      (svc/calculate-with-costs (:component/service sut) 1 :operations/division [100 10])

      (let [result (svc/calculate-with-costs (:component/service sut)
                                             1
                                             :operations/division
                                             [100 10])]
        (is (= {:response-type :error
                :error "Insufficient balance: Unable to withdraw 1.00. Current balance is 0.50."}
               result))

        (is (= {:id 6
                :username "john.doe@example.com"
                :type "division"
                :amount 1.00M
                :user_balance 0.50M
                :operation_result "{:exception \"Insufficient balance: Unable to withdraw 1.00. Current balance is 0.50.\"}"}
               (-> (get-operations-records (:component/db.mysql sut) 1 0 10)
                   last
                   (dissoc :created_at))))))))

(deftest tests-failed-calculation-with-cost
  (with-system [sut (service-system)]
    (testing "Should return calculation result with error for division by zero"
      (let [result (svc/calculate-with-costs (:component/service sut)
                                             1
                                             :operations/division
                                             [50 0])]
        (is (= {:response-type :error
                :error "Calculation error: Divide by zero"}
               result))

        (is (= {:id 1
                :username "john.doe@example.com"
                :type "division"
                :amount 1.00M
                :user_balance 5.00M
                :operation_result "{:exception \"Calculation error: Divide by zero\"}"}
               (-> (get-operations-records (:component/db.mysql sut) 1 0 10)
                   last
                   (dissoc :created_at))))))))
