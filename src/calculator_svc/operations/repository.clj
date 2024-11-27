(ns calculator-svc.operations.repository)

(defprotocol OperationsRepository
  (test-connection [connectable])
  (get-operation-cost [connectable])
  (record-operation [connectable user-id operation-id amount user-balance response]))
