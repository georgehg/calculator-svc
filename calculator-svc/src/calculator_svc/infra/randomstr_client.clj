(ns calculator-svc.infra.randomstr-client
  (:require
   [clj-http.conn-mgr :as conn-mgr]
   [clj-http.core :as http]
   [com.stuartsierra.component :as component]))

(defrecord RandomSTRClient [url default-request]
  component/Lifecycle
  (start [this]
    (let [cm              (conn-mgr/make-reusable-conn-manager {:timeout 5 :threads 5 :default-per-route 5})
          http-client     (http/build-http-client {} false cm)
          default-request {:as                 :auto ;coercing body type for client/coerce-response-body
                           :socket-timeout     120000
                           :connection-timeout 120000
                           ;:debug              true
                           :http-client        http-client
                           :connection-manager cm}]
      (assoc this :default-request default-request)))

  (stop [this]
    (conn-mgr/shutdown-manager (:connection-manager default-request))
    (assoc this :default-request nil)))

(defn new-random-str-client
  ([]
   (new-random-str-client "https://www.random.org/strings/?num=1&len=20&digits=on&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new"))
  ([url]
   (map->RandomSTRClient {:url url})))
