(ns calculator-svc.infra.mysql-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [sut]}}}}
  (:require
   [calculator-svc.containers.mysql-container :as container]
   [calculator-svc.helpers :refer [with-system]]
   [calculator-svc.infra.mysql :refer [new-mysql]]
   [calculator-svc.operations.repository :as repository]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [com.stuartsierra.component :as component]))

(defn- db-system []
  (let [config container/config+container]
    (component/system-map
     :component/db.mysql (new-mysql {:mysql (:mysql config)
                                     :migration (:migration config)}))))

(use-fixtures :once container/mysql-fixtures)

(deftest tests-connection-status
  (with-system [sut (db-system)]
    (testing "Test Postgres database connection status"
      (let [db-status (repository/test-connection (:component/db.mysql sut))]
        (is (= {:status "OK"} db-status))))))

(deftest tests-get-operation-costs
  (with-system [sut (db-system)]
    (testing "Test Postgres database connection status"
      (is (= {:cost 0.50M} (repository/get-operation-cost (:component/db.mysql sut) :addition))))))
