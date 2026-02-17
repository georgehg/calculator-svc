(ns calculator-svc.users.repository
  (:require
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
  (:import
   (calculator_svc.infra.mysql_adapter MySQL)))

(defprotocol UsersRepository
  (get-user [sourceable user-id])
  (get-user-by-email [sourceable email])
  (withdraw-balance [sourceable user-id amount]))

(extend-type MySQL
  UsersRepository
  (get-user [sourceable user-id]
    (first
     (sql/query sourceable
                ["SELECT id, username, status, user_balance
                  FROM operations.user
                  WHERE id = ?
                  AND deleted_at IS NULL"
                 user-id]
                {:builder-fn rs/as-unqualified-lower-maps})))

  (get-user-by-email [sourceable email]
    (first
     (sql/query sourceable
                ["SELECT id, username, status, user_balance
                  FROM operations.user
                  WHERE username = ?
                  AND deleted_at IS NULL"
                 email]
                {:builder-fn rs/as-unqualified-lower-maps})))

  (withdraw-balance [sourceable user-id amount]
    (when-not (get-user sourceable user-id)
      (throw (ex-info "User not found." {})))

    (let [updated
          (-> (sql/query sourceable
                         ["UPDATE operations.user
                           SET user_balance = user_balance - ?
                           WHERE id = ?
                           AND user_balance >= ?
                           AND deleted_at IS NULL"
                          amount
                          user-id
                          amount])
              first
              :next.jdbc/update-count)]

      (case updated
        0 (throw (ex-info (str "Insufficient balance: Unable to withdraw "
                               amount
                               ". Current balance is "
                               (:user_balance (get-user sourceable user-id))
                               ".")
                          {}))
        1 (first (sql/find-by-keys sourceable
                                   :operations.user
                                   {:id user-id}
                                   {:columns [:user_balance]
                                    :builder-fn rs/as-unqualified-lower-maps}))))))
