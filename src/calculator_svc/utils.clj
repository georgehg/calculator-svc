(ns calculator-svc.utils
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]))

(defn- config [env] (read-config (io/resource "config.edn") {:profile env}))

(defn get-config
  ([]
   (let [profile (or (keyword (System/getenv "ENV"))
                     (keyword (System/getProperty "config.profile"))
                     :local)
         config (config profile)]
     config))
  ([opt-profile]
   (config opt-profile)))
