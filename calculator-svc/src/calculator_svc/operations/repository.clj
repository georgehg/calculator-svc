(ns calculator-svc.operations.repository
  (:require
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
  (:import
   (calculator_svc.infra.mysql_adapter MySQL)))

(defprotocol OperationsRepository
  (get-operation-cost [sourceable operation])
  (record-operation [sourceable user-id operation-id cost user-balance result])
  (get-operations-records [sourceable user-id page-number page-limit])
  (count-operations [sourceable user-id])
  (delete-operation [sourceable operation-id]))

(extend-type MySQL
  OperationsRepository
  (get-operation-cost [sourceable operation]
    (first (sql/find-by-keys sourceable
                             :operations.operation
                             {:type (name operation)}
                             {:columns [:id :cost]
                              :builder-fn rs/as-unqualified-lower-maps})))

  (record-operation [sourceable user-id operation-id cost user-balance result]
    (:GENERATED_KEY (sql/insert! sourceable
                                 :operations.record
                                 {:user_id user-id
                                  :operation_id operation-id
                                  :amount cost
                                  :user_balance user-balance
                                  :operation_result (str result)})))

  (get-operations-records [sourceable user-id page-number page-limit]
    (sql/query sourceable
               ["SELECT r.id, u.username, o.type, r.amount, r.user_balance,
                        r.operation_result, r.created_at
                 FROM operations.record r
                 JOIN operations.user u ON u.id = r.user_id
                 JOIN operations.operation o ON o.id = r.operation_id
                 WHERE r.user_id = ?
                 AND r.deleted_at IS NULL
                 ORDER BY r.created_at DESC
                 LIMIT ?, ?"
                user-id
                page-number
                page-limit]
               {:builder-fn rs/as-unqualified-lower-maps}))

  (count-operations [sourceable user-id]
    (-> (sql/query sourceable
                   ["SELECT COUNT(*) as count
                     FROM operations.record
                     WHERE user_id = ?
                     AND deleted_at IS NULL"
                    user-id]
                   {:builder-fn rs/as-unqualified-lower-maps})
        first
        :count))

  (delete-operation [sourceable operation-id]
    (sql/update! sourceable
                 :operations.record
                 {:deleted_at (java.time.Instant/now)}
                 {:id operation-id
                  :deleted_at nil})))
