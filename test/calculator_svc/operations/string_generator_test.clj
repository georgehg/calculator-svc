(ns calculator-svc.operations.string-generator-test
  (:require
   [calculator-svc.containers.mockserver-container :refer [container->url
                                                           create-container
                                                           start-container
                                                           stop-container]]
   [calculator-svc.infra.randomstr-client :refer [new-random-str-client]]
   [calculator-svc.operations.string-generator :as sut]
   [clojure.test :refer [deftest is testing use-fixtures]]))

(defonce mockserver-container (create-container "mockserver_initializer.json"))

(defn mockserver-fixtures [f]
  (alter-var-root #'mockserver-container start-container)
  (try
    (f)
    (finally
      (alter-var-root #'mockserver-container stop-container))))

(use-fixtures :once mockserver-fixtures)

(deftest addition-operation-test
  (testing "Tests random string generation"
    (is (= {:success "wdJoaGkPbKlD2AYJQE9I"}
           (sut/gen-string (new-random-str-client (str (container->url mockserver-container) "/strings"))))))

  (testing "Tests random string generation with erors"
    (is (= {:error "The minimum value must be an integer"}
           (sut/gen-string (new-random-str-client (str (container->url mockserver-container) "/integers")))))))
