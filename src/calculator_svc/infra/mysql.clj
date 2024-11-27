(ns calculator-svc.infra.mysql
  (:require
   [calculator-svc.operations.repository :as repository]
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as rs])
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
  (test-connection [this]
    (jdbc/execute-one! (:connection this) ["Select 'OK' as status"]
                       {:builder-fn rs/as-unqualified-lower-maps})))

(defn new-mysql
  [config]
  (map->MySQL {:config config}))
