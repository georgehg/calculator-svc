(ns calculator-svc.uers.repository-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [sut]}}}}
  (:require
   [calculator-svc.containers.mysql-container :as container]
   [calculator-svc.helpers :refer [with-system]]
   [calculator-svc.infra.mysql-adapter :refer [new-mysql]]
   [calculator-svc.users.repository :as sut]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [com.stuartsierra.component :as component]))

(defn- db-system []
  (let [config container/config+container]
    (component/system-map
     :component/db.mysql (new-mysql {:mysql (:mysql config)
                                     :migration (:migration config)}))))

(use-fixtures :each container/mysql-fixtures)

(deftest tests-get-and-find-users
  (with-system [sut (db-system)]
    (testing "Tests get user by id"
      (is (= {:id 1
              :status "active"
              :user_balance 10.00M
              :username "john.doe@example.com"}
             (sut/get-user (:component/db.mysql sut) 1)))

      (is (= {:id 4
              :status "active"
              :user_balance 300.00M
              :username "bob.builder@example.com"}
             (sut/get-user-by-email (:component/db.mysql sut) "bob.builder@example.com"))))))

(deftest tests-withdraw-user-amount
  (with-system [sut (db-system)]
    (testing "Tests withdraw balance with success"
      (is (= {:user_balance 9.00M}
             (sut/withdraw-balance (:component/db.mysql sut) 1 1M))))

    (testing "Tests withdraw balance with failure beucase balance "
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Insufficient balance: Unable to withdraw 51. Current balance is 50.75."
           (sut/withdraw-balance (:component/db.mysql sut) 2 51M))))

    (testing "Tests withdraw balance with failure because user not found"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"User not found."
           (sut/withdraw-balance (:component/db.mysql sut) 10 51M))))))
