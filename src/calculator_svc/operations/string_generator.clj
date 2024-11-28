(ns calculator-svc.operations.string-generator
  (:require
   [clj-http.client :as http]
   [clojure.string :refer [trim-newline]]
   [clojure.tools.logging :as log])
  (:import
   (calculator_svc.infra.randomstr_client RandomSTRClient)))

(defprotocol StringGenerator
  (random-string [client]))

(extend-type RandomSTRClient
  StringGenerator
  (random-string [client]
    (try
      (let [{:keys [body]} (http/get (:url client) (:default-request client))]
        {:success (trim-newline body)})

      (catch Exception ex
        (let [{:keys [status body request-time]}
              (select-keys (ex-data ex) [:request-time :status :body])]
          (log/error :response {:status       status
                                :url          (:url client)
                                :body         body
                                :request-time request-time})
          {:error (or body (ex-message ex))})))))
