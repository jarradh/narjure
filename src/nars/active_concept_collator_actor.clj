(ns nars.active-concept-collator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [nars.logger :refer [logger]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare active-concept-collator-actor)

(defn process-inference-tick [state]
  (! :logger [:log-msg :log-debug (str "process-inference-tick")])
  {})

(defn process-active-concept [_ _]
  (! :logger [:log-msg :log-debug (str "process-active-concept")]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In active-concept-collator :else")]))

(defn active-concept-collator-actor
        "state is collection of active concepts"
        [in-state]
        (register! :active-concept-collator @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   :inference-tick-msg (set-state! (process-inference-tick @state))
                   [:active-concept-msg active-concept] (process-active-concept active-concept @state)
                   :else (process-unhandled-msg msg))
          (recur)))
