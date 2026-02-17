(ns calculator-svc.infra.mysql-adapter
  (:require
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection])
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
  [datasource migration-config]
  (merge {:db {:datasource (jdbc/get-datasource datasource)}}
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

(defrecord MySQL [datasource config]
  component/Lifecycle
  (start [this]
    (if-not (:datasource this)
      (let [db-spec (config->db-spec (:mysql config))
            datasource (connection/->pool HikariDataSource db-spec)]
        (execute-migration (config->migration-spec datasource (:migration config)))
        (log/info :started :mysql-datasource :database (-> config :mysql :database))
        (assoc this :datasource datasource))
      this))

  (stop [this]
    (when-let [datasource (:datasource this)]
      (.close datasource)
      (log/info :stopped :mysql-datasource :database (-> config :mysql :database))
      (dissoc this :datasource :config))))

(defn new-mysql
  [config]
  (map->MySQL {:config config}))

(defmacro with-mysql-transaction
  [[sym db] & body]
  `(jdbc/with-transaction
     [~sym ~db]
     (let [~sym (assoc ~db :datasource ~sym)]
       ~@body)))
