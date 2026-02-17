(ns calculator-svc.containers.mockserver-container
  (:require [clj-test-containers.core :as tc]))

(defn create-container
  [init-file]
  (-> (tc/create {:image-name    "mockserver/mockserver:5.15.0"
                  :exposed-ports [1080]
                  :env-vars      {"MOCKSERVER_PROPERTY_FILE" "/config/mockserver.properties",
                                  "MOCKSERVER_INITIALIZATION_JSON_PATH" (str "/config/" init-file)}
                  :wait-for      {:wait-strategy   :http
                                  :path            "/health"
                                  :port            1080
                                  :method          "GET"
                                  :status-codes    [200]
                                  :tls             false
                                  :read-timout     5
                                  :headers         {"Accept" "application/json"}
                                  :startup-timeout 60}})
      (tc/bind-filesystem! {:host-path      "test/calculator_svc/containers"
                            :container-path "/config"
                            :mode           :read-only})))

(defn start-container
  [container]
  (tc/start! container))

(defn stop-container
  [container]
  (tc/stop! container))

(defn container->url
  [container]
  (str "http://localhost:" (str (get (:mapped-ports container) 1080))))
