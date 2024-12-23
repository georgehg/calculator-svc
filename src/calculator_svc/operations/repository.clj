(ns calculator-svc.operations.repository
  (:require
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
  (:import
   (calculator_svc.infra.mysql_adapter MySQL)))

(defprotocol OperationsRepository
  (get-operation-cost [connectable operation])
  (record-operation [connectable user-id operation-id cost user-balance result])
  (get-operations-records [connectable user-id page-number page-limit])
  (count-operations [connectable user-id])
  (delete-operation [connectable operation-id]))

(extend-type MySQL
  OperationsRepository
  (get-operation-cost [this operation]
    (first (sql/find-by-keys (:connection this)
                             :operations.operation
                             {:type (name operation)}
                             {:columns [:id :cost]
                              :builder-fn rs/as-unqualified-lower-maps})))

  (record-operation [this user-id operation-id cost user-balance result]
    (:GENERATED_KEY (sql/insert! (:connection this)
                                 :operations.record
                                 {:user_id user-id
                                  :operation_id operation-id
                                  :amount cost
                                  :user_balance user-balance
                                  :operation_result (str result)})))

  (get-operations-records [this user-id page-number page-limit]
    (sql/query (:connection this)
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

  (count-operations [this user-id]
    (-> (sql/query (:connection this)
                   ["SELECT COUNT(*) as count
                     FROM operations.record
                     WHERE user_id = ?
                     AND deleted_at IS NULL"
                    user-id]
                   {:builder-fn rs/as-unqualified-lower-maps})
        first
        :count))

  (delete-operation [this operation-id]
    (sql/update! (:connection this)
                 :operations.record
                 {:deleted_at (java.time.Instant/now)}
                 {:id operation-id
                  :deleted_at nil})))
