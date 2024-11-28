(ns calculator-svc.operations.repository)

(defprotocol OperationsRepository
  (test-connection [connectable])
  (get-operation-cost [connectable operation])
  (record-operation [connectable user-id operation-id cost user-balance response])
  (get-operations-records [connectable user-id page-number page-limit])
  (count-operations [connectable user-id])
  (delete-operation [connectable id]))
