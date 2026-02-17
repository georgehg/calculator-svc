(ns calculator-svc.containers.mysql-container
  (:require
   [calculator-svc.utils :refer [get-config]]
   [clj-test-containers.core :as tc]))

(defn- create-container
  []
  (tc/create {:image-name    "mysql:8.0"
              :exposed-ports [3306]
              :env-vars      {"MYSQL_ROOT_PASSWORD" "arithmetic"
                              "MYSQL_DATABASE" "calculatordb"}
              :wait-for      {:log "port: 3306  MySQL Community Server - GPL"}}))

(defonce mysql-container (create-container))

(defn- container->config
  [config]
  (update config
          :mysql
          assoc :port (get (:mapped-ports mysql-container) 3306)))

(defonce config+container (get-config :test))

(defn start-container
  [container]
  (tc/start! container))

(defn stop-container
  [container]
  (tc/stop! container))

(defn mysql-fixtures
  [f]
  (alter-var-root #'mysql-container start-container)
  (alter-var-root #'config+container container->config)
  (try
    (f)
    (finally (alter-var-root #'mysql-container stop-container))))
