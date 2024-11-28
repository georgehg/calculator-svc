(ns calculator-svc.operations.arithmetic
  (:require
   [clojure.math :refer [sqrt]]))

(def operations
  {:addition +
   :subtraction -
   :multiplication *
   :division /
   :square-root sqrt})

(defn calculates
  "Execute arithmetic calculation with given operation and args"
  [ops & args]
  {:pre [(contains? operations ops)]}
  (try
    (let [result (apply (ops operations) args)]
      (if (NaN? result)
        {:error result}
        {:success result}))
    (catch Exception ex
      {:error (ex-message ex)})))
