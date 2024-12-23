(ns calculator-svc.operations.repository-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [sut]}}}}
  (:require
   [calculator-svc.containers.mysql-container :as container]
   [calculator-svc.helpers :refer [with-system]]
   [calculator-svc.infra.mysql-adapter :refer [new-mysql]]
   [calculator-svc.operations.repository :as ops-repo]
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
  [sut user-id operation-id cost user-balance result]
  (ops-repo/record-operation sut
                             user-id
                             operation-id
                             cost
                             (- user-balance cost)
                             result))

(deftest tests-get-operation-costs
  (with-system [sut (db-system)]
    (testing "Tests query for operation cost"
      (is (= {:id 1 :cost 0.50M} (ops-repo/get-operation-cost (:component/db.mysql sut) :operations/addition))))))

(deftest tests-records-operation
  (with-system [sut (db-system)]
    (testing "Tests record an operation processing"
      (let [con       (-> sut :component/db.mysql :connection)
            user      (sql/get-by-id con :operations.user 1)
            operation (ops-repo/get-operation-cost (:component/db.mysql sut) :operations/addition)
            record-id (record-operation (:component/db.mysql sut)
                                        (:user/id user)
                                        (:id operation)
                                        (:cost operation)
                                        (:user/user_balance user)
                                        {:success 15})]
        (is (= {:id 1
                :user_id 1
                :operation_id 1
                :amount 0.50M
                :user_balance 9.50M
                :operation_result "{:success 15}"}
               (-> (sql/get-by-id con :operations.record
                                  record-id
                                  {:builder-fn rs/as-unqualified-lower-maps})
                   (select-keys [:id :user_id :operation_id :amount :user_balance :operation_result]))))))

    (testing "Tests query for paginated operation records"
      (let [operation (ops-repo/get-operation-cost (:component/db.mysql sut) :operations/multiplication)]
        (record-operation (:component/db.mysql sut)
                          1
                          (:id operation)
                          (:cost operation)
                          9.50M
                          {:success 30})
        (is (= 2 (count (ops-repo/get-operations-records (:component/db.mysql sut) 1 0 2))))
        (is (= {:id 2
                :username "john.doe@example.com"
                :type "multiplication"
                :amount 0.75M
                :user_balance 8.75M
                :operation_result "{:success 30}"}
               (-> (ops-repo/get-operations-records (:component/db.mysql sut) 1 0 2)
                   second
                   (dissoc :created_at))))))

    (testing "Tests query for count operation records"
      (let [operation (ops-repo/get-operation-cost (:component/db.mysql sut) :square_root)]
        (record-operation (:component/db.mysql sut)
                          1
                          (:id operation)
                          (:cost operation)
                          8.75M
                          {:success 3})
        (is (= 3 (ops-repo/count-operations (:component/db.mysql sut) 1)))))

    (testing "Tests soft delete operation"
      (is (= 3 (ops-repo/count-operations (:component/db.mysql sut) 1)))
      (ops-repo/delete-operation (:component/db.mysql sut) 1)
      (is (= 2 (ops-repo/count-operations (:component/db.mysql sut) 1))))))
