(ns calculator-svc.operations.operations
  (:require
   [clojure.math :refer [sqrt]]
   [clojure.tools.logging :as log]))

(def operations
  {:addition +
   :subtraction -
   :multiplication *
   :division /
   :square-root sqrt})

(defn calculates
  [ops & args]
  {:pre [(contains? operations ops)]}
  (try
    (let [result (apply (ops operations) args)]
      (if (NaN? result)
        {:error result}
        {:result result}))
    (catch Exception ex
      (log/error :unsupported-operation {:operation ops :args args :exception ex})
      {:error (ex-message ex)})))
