(ns narjure.memory-management.event-buffer
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.bag :as b]
    [narjure.global-atoms :refer :all]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :event-buffer)
(def display (atom '()))
(def search (atom ""))

(defn event-handler
  ""
  [from [_ task]]
  ;todo
  (try
    (swap! e-bag b/add-element {:id task :priority (first (:budget task))})
    (catch Exception e (debuglogger search display (str "event add error " (.toString e))))))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [aname actor-ref]
  (reset! display '())
  (register! aname actor-ref)
  (set-state! {}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger search display message)
  (case type
    :event-msg (event-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn event-buffer []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))
