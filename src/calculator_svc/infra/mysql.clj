(ns calculator-svc.infra.mysql
  (:require
   [calculator-svc.operations.repository :as repository]
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(defn- execute-migration [config]
  (migratus/init config)
  (as-> (migratus/migrate config) completed
    (log/info :migration-completed (or completed :successful)))

    ;;## Keeping it here only for tests purposes
    ;;## (migratus/rollback config)
  )

(defn- config->migration-spec
  [connection migration-config]
  (merge {:db {:datasource (jdbc/get-datasource connection)}}
         migration-config
         {:store :database}))

(defn- config->db-spec
  [{:keys [database host port username password socket-timeout-seconds maximum-pool-size]}]
  {:dbtype   "mysql"
   :dbname   database
   :host     host
   :port     port
   :username username
   :password password
   :dataSourceProperties {:socketTimeout   socket-timeout-seconds
                          :maximumPoolSize maximum-pool-size}})

(defrecord MySQL [connection config]
  component/Lifecycle
  (start [this]
    (if-not (:connection this)
      (let [db-spec (config->db-spec (:mysql config))
            connection (connection/->pool HikariDataSource db-spec)]
        (execute-migration (config->migration-spec connection (:migration config)))
        (log/info :started :mysql-connection :database (-> config :mysql :database))
        (assoc this :connection connection))
      this))

  (stop [this]
    (when-let [connection (:connection this)]
      (.close connection)
      (log/info :stopped :mysql-connection :database (-> config :mysql :database))
      (dissoc this :connection :config)))

  repository/OperationsRepository
  (test-connection [_]
    (jdbc/execute-one! connection ["Select 'OK' as status"]
                       {:builder-fn rs/as-unqualified-lower-maps}))

  (get-operation-cost [_ operation]
    (first (sql/find-by-keys connection
                             :operations.operation
                             {:type (name operation)}
                             {:columns [:id :cost]
                              :builder-fn rs/as-unqualified-lower-maps})))

  (record-operation [_ user-id operation-id cost user-balance response]
    (:GENERATED_KEY (sql/insert! connection
                                 :operations.record
                                 {:user_id user-id
                                  :operation_id operation-id
                                  :amount cost
                                  :user_balance user-balance
                                  :operation_response (name response)})))

  (get-operations-records [_ user-id page-number page-limit]
    (sql/query connection
               ["SELECT r.id, u.username, o.type, r.amount, r.user_balance, r.operation_response, r.created_at
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

  (count-operations [_ user-id]
    (-> (sql/query connection
                   ["SELECT COUNT(*) as count
                     FROM operations.record
                     WHERE user_id = ?"
                    user-id]
                   {:builder-fn rs/as-unqualified-lower-maps})
        first
        :count)))

(defn new-mysql
  [config]
  (map->MySQL {:config config}))
