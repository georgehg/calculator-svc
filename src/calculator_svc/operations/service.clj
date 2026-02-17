(ns calculator-svc.operations.service
  {:clj-kondo/config '{:linters {:unresolved-symbol {:exclude [tx]}}}}
  (:require
   [calculator-svc.infra.mysql-adapter :refer [with-mysql-transaction]]
   [calculator-svc.operations.arithmetic :refer [calculates]]
   [calculator-svc.operations.repository :as ops-repo]
   [calculator-svc.users.repository :as users-repo]
   [clojure.tools.logging :as log]))

(defprotocol Calculator
  (calculate-with-costs [this user-id operation args]))

(defrecord CalculatorService [db]
  Calculator
  (calculate-with-costs
    [_ user-id operation args]
    (let [{:keys [id cost]} (ops-repo/get-operation-cost db operation)]
      (try
        (with-mysql-transaction [tx db]
          (let [{:keys [user_balance]} (users-repo/withdraw-balance tx user-id cost)
                result (apply calculates operation args)]

            (if (:success result)
              (do
                (ops-repo/record-operation tx user-id id cost user_balance result)
                (log/info :calculation/success {:user-id user-id :operation operation :cost cost})
                {:response-type :success
                 :result (:success result)
                 :cost cost
                 :user-balance user_balance})
              (throw (ex-info (str "Calculation error: " (:error result)) {})))))

        (catch Exception ex
          (log/error :calculation/failed {:user-id user-id :message (ex-message ex) :exception ex})
          (ops-repo/record-operation db user-id id cost
                                     (:user_balance (users-repo/get-user db user-id))
                                     {:exception (ex-message ex)})
          {:response-type :error
           :error (ex-message ex)})))))

(defn new-calculator-service
  []
  (map->CalculatorService {}))
