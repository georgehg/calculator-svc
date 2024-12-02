(ns calculator-svc.infra.probe
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs])
  (:import
   (calculator_svc.infra.mysql_adapter MySQL)))

(defprotocol ProbesTest
  (test-connection [connectable]))

(extend-type MySQL
  ProbesTest
  (test-connection [this]
    (jdbc/execute-one! (:connection this) ["Select 'OK' as status"]
                       {:builder-fn rs/as-unqualified-lower-maps})))
