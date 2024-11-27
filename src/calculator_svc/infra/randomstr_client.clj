(ns calculator-svc.infra.randomstr-client
  (:require
   [calculator-svc.operations.string-generator :as str-gen]
   [clj-http.client :as client]
   [clj-http.conn-mgr :as conn-mgr]
   [clj-http.core :as http]
   [clojure.string :refer [trim-newline]]
   [clojure.tools.logging :as log]
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
    (assoc this :default-request nil))

  str-gen/StringGenerator
  (random-string [_]
    (try
      (let [{:keys [body]} (client/get url default-request)]
        {:success (trim-newline body)})

      (catch Exception ex
        (let [{:keys [status body request-time]}
              (select-keys (ex-data ex) [:request-time :status :body])]
          (log/error :response {:status       status
                                :url          url
                                :body         body
                                :request-time request-time})
          {:error (ex-message ex)})))))

(defn new-random-str-client
  ([]
   (new-random-str-client "https://www.random.org/strings/?num=1&len=20&digits=on&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new"))
  ([url]
   (map->RandomSTRClient {:url url})))
