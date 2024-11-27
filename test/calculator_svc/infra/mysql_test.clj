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

(defn- config->db-spec
  [{:keys [database host port username password]}]
  {:dbtype   "mysql"
   :dbname   database
   :host     host
   :port     port
   :user     username
   :password password})

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
  (testing "Tests record an operation processing"
    (with-system [sut (db-system)]
      (let [con       (-> sut :component/db.mysql :connection)
            user      (->> (sql/insert! con :operations.user
                                        {:username "John Smith" :password "secret" :user_balance 10.0M})
                           :GENERATED_KEY
                           (sql/get-by-id con :operations.user))
            operation (repository/get-operation-cost (:component/db.mysql sut) :addition)
            record-id (repository/record-operation (:component/db.mysql sut)
                                                   (:user/id user)
                                                   (:id operation)
                                                   (:cost operation)
                                                   (:user/user_balance user)
                                                   :success)]
        (is (= {:id 1
                :user_id 1
                :operation_id 1
                :amount 0.50M
                :user_balance 9.50M
                :operation_response "success"}
               (-> (sql/get-by-id con :operations.record
                                  record-id
                                  {:builder-fn rs/as-unqualified-lower-maps})
                   (select-keys [:id :user_id :operation_id :amount :user_balance :operation_response]))))))))
