(ns calculator-svc.users.repository
  (:require
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
  (:import
   (calculator_svc.infra.mysql_adapter MySQL)))

(defprotocol UsersRepository
  (get-user [connectable user-id])
  (get-user-by-email [connectable email])
  (withdraw-balance [connectable user-id amount]))

(extend-type MySQL
  UsersRepository
  (get-user [this user-id]
    (first
     (sql/query (:connection this)
                ["SELECT id, username, status, user_balance
                  FROM operations.user
                  WHERE id = ?
                  AND deleted_at IS NULL"
                 user-id]
                {:builder-fn rs/as-unqualified-lower-maps})))

  (get-user-by-email [this email]
    (first
     (sql/query (:connection this)
                ["SELECT id, username, status, user_balance
                  FROM operations.user
                  WHERE username = ?
                  AND deleted_at IS NULL"
                 email]
                {:builder-fn rs/as-unqualified-lower-maps})))

  (withdraw-balance [this user-id amount]
    (when-not (get-user this user-id)
      (throw (ex-info "User not found." {})))

    (let [updated
          (-> (sql/query (:connection this)
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
                               (:user_balance (get-user this user-id))
                               ".")
                          {}))
        1 (first (sql/find-by-keys (:connection this)
                                   :operations.user
                                   {:id user-id}
                                   {:columns [:user_balance]
                                    :builder-fn rs/as-unqualified-lower-maps}))))))
