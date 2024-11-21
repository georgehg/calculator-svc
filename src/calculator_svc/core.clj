(ns calculator-svc.core)

(defn start-service
  []
  (println "starting service"))

(defn stop-service
  []
  (println "stopping service"))

(defn -main [& _args]
  (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC"))
  (start-service)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(stop-service))))

(comment
  ;; Evaluate the do block below to start the HTTP server and kafka consumers.
  ;; Reevaluate it to restart it quickly. Whenever you change a file,
  ;; reload the changed file and then reevaluate this block
  (do   (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC"))
        (load-file "src/calculator_svc/core.clj")
        (stop-service)
        (start-service)
        nil))
