(ns calculator-svc.infra.mysql-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [sut]}}}}
  (:require
   [calculator-svc.containers.mysql-container :as container]
   [calculator-svc.helpers :refer [with-system]]
   [calculator-svc.infra.mysql :refer [new-mysql]]
   [calculator-svc.operations.repository :as repository]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [com.stuartsierra.component :as component]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn- db-system []
  (let [config container/config+container]
    (component/system-map
     :component/db.mysql (new-mysql {:mysql (:mysql config)
                                     :migration (:migration config)}))))

(use-fixtures :once container/mysql-fixtures)

(defn- record-operation
  [sut user-id operation-id cost user-balance]
  (repository/record-operation sut
                               user-id
                               operation-id
                               cost
                               (- user-balance cost)
                               :success))

(deftest tests-connection-status
  (with-system [sut (db-system)]
    (testing "Test MySQL database connection status"
      (let [db-status (repository/test-connection (:component/db.mysql sut))]
        (is (= {:status "OK"} db-status))))))

(deftest tests-get-operation-costs
  (with-system [sut (db-system)]
    (testing "Tests query for operation cost"
      (is (= {:id 1 :cost 0.50M} (repository/get-operation-cost (:component/db.mysql sut) :addition))))))

(deftest tests-records-operation
  (with-system [sut (db-system)]
    (testing "Tests record an operation processing"
      (let [con       (-> sut :component/db.mysql :connection)
            user      (sql/get-by-id con :operations.user 1)
            operation (repository/get-operation-cost (:component/db.mysql sut) :addition)
            record-id (record-operation (:component/db.mysql sut)
                                        (:user/id user)
                                        (:id operation)
                                        (:cost operation)
                                        (:user/user_balance user))]
        (is (= {:id 1
                :user_id 1
                :operation_id 1
                :amount 0.50M
                :user_balance 9.50M
                :operation_response "success"}
               (-> (sql/get-by-id con :operations.record
                                  record-id
                                  {:builder-fn rs/as-unqualified-lower-maps})
                   (select-keys [:id :user_id :operation_id :amount :user_balance :operation_response]))))))

    (testing "Tests query for paginated operation records"
      (let [operation (repository/get-operation-cost (:component/db.mysql sut) :multiplication)]
        (record-operation (:component/db.mysql sut)
                          1
                          (:id operation)
                          (:cost operation)
                          9.50M)
        (is (= 2 (count (repository/get-operations-records (:component/db.mysql sut) 1  0 2))))
        (is (= {:id 1
                :username "john.doe@example.com"
                :type "addition"
                :amount 0.50M
                :user_balance 9.50M
                :operation_response "success"}
               (-> (repository/get-operations-records (:component/db.mysql sut) 1  0 1)
                   first
                   (dissoc :created_at))))))

    (testing "Tests query for count operation records"
      (let [operation (repository/get-operation-cost (:component/db.mysql sut) :square_root)]
        (record-operation (:component/db.mysql sut)
                          1
                          (:id operation)
                          (:cost operation)
                          8.75M)
        (is (= 3 (repository/count-operations (:component/db.mysql sut) 1)))))

    (testing "Tests soft delete operation"
      (is (= 3 (repository/count-operations (:component/db.mysql sut) 1)))
      (repository/delete-operation (:component/db.mysql sut) 1)
      (is (= 2 (repository/count-operations (:component/db.mysql sut) 1))))))
