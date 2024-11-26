(ns calculator-svc.operations.string-generator)

(defprotocol StringGenerator
  (random-string [this]))

(defn gen-string
  [this]
  (random-string this))
