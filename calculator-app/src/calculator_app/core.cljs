(ns calculator-app.core
  (:require [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            ["react-dom/client" :as rdom]))

(defn submit-operation
  ([operation arg1]
   (submit-operation operation arg1 nil))
  ([operation arg1 arg2]
   (js/fetch "/api/v1/operations"
             (clj->js {:headers  {:Content-Type "application/json"
                                  :authorization "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"}
                       :method "POST"
                       :body (js/JSON.stringify #js {:operation (name operation)
                                                     :arg1 arg1
                                                     :arg2 arg2})}))))

(defnc addition-operation
  []
  (let [[state set-state] (hooks/use-state {:arg1 0 :arg2 0 :operation :addition :result 0})]
    (d/div (d/strong "Addition: ")
           (d/input {:value (:arg1 state)
                     :on-change #(set-state assoc :arg1 (.. % -target -value))})
           (d/strong " + ")
           (d/input {:value (:arg2 state)
                     :on-change #(set-state assoc :arg2 (.. % -target -value))})
           (d/button {:on-click #(submit-operation :addition (:arg1 state) (:arg2 state))}
                     "GO! =")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))}))))

(defnc subtraction-operation
  []
  (let [[state set-state] (hooks/use-state {:arg1 0 :arg2 0 :operation :subtraction :result 0})]
    (d/div (d/strong "Subtraction: ")
           (d/input {:value (:arg1 state)
                     :on-change #(set-state assoc :arg1 (.. % -target -value))})
           (d/strong " - ")
           (d/input {:value (:arg2 state)
                     :on-change #(set-state assoc :arg2 (.. % -target -value))})
           (d/button {:on-click #(submit-operation :subtraction (:arg1 state) (:arg2 state))}
                     "GO! =")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))}))))

(defnc multiplication-operation
  []
  (let [[state set-state] (hooks/use-state {:arg1 0 :arg2 0 :operation :multiplication :result 0})]
    (d/div (d/strong "Multiplication: ")
           (d/input {:value (:arg1 state)
                     :on-change #(set-state assoc :arg1 (.. % -target -value))})
           (d/strong " * ")
           (d/input {:value (:arg2 state)
                     :on-change #(set-state assoc :arg2 (.. % -target -value))})
           (d/button {:on-click #(submit-operation :multiplication (:arg1 state) (:arg2 state))}
                     "GO! =")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))}))))

(defnc division-operation
  []
  (let [[state set-state] (hooks/use-state {:arg1 0 :arg2 0 :operation :division :result 0})]
    (d/div (d/strong "Division: ")
           (d/input {:value (:arg1 state)
                     :on-change #(set-state assoc :arg1 (.. % -target -value))})
           (d/strong " / ")
           (d/input {:value (:arg2 state)
                     :on-change #(set-state assoc :arg2 (.. % -target -value))})
           (d/button {:on-click #(submit-operation :division (:arg1 state) (:arg2 state))}
                     "GO! =")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))}))))

(defnc squareroot-operation
  []
  (let [[state set-state] (hooks/use-state {:arg1 0 :operation :squareroot :result 0})]
    (d/div (d/strong "Square Root: ")
           (d/input {:value (:arg1 state)
                     :on-change #(set-state assoc :arg1 (.. % -target -value))})
           (d/button {:on-click #(submit-operation :squareroot (:arg1 state) (:arg2 state))}
                     "GO! =")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))}))))

(defnc randomstring-operation
  []
  (let [[state set-state] (hooks/use-state {:operation :squareroot :result ""})]
    (d/div (d/strong "Random String: ")
           (d/input {:value (:result state)
                     :on-change #(set-state assoc :result (.. % -target -value))})
           (d/button {:on-click #(submit-operation :squareroot (:arg1 state) (:arg2 state))}
                     "GO! ="))))

(defnc app []
  (d/div
   (d/h1 "Arithmetic Calculator")
   (d/div
    (d/h3 "Operations"))
   (d/div
    ($ addition-operation)
    ($ subtraction-operation)
    ($ multiplication-operation)
    ($ division-operation)
    ($ squareroot-operation)
    ($ randomstring-operation))))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ app))
  (js/console.log "Teste"))
