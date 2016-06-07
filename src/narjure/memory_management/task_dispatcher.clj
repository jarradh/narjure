(ns narjure.memory-management.task-dispatcher
  (:use [co.paralleluniverse.pulsar core actors])
  (:require
    [narjure.global-atoms :refer [c-bag]]
    [narjure.bag :as b]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :task-dispatcher)

(defn event?
  "return true if task is event otherwise false"
  [{:keys [occurrence]}]
  (not= occurrence :eternal))

(defn term-exists?
  ""
  [term]
  (b/exists? @c-bag term))

(defn task-handler
  "If concept, or any sub concepts, do not exist post task to concept-creator,
   otherwise, dispatch task to respective concepts. Also, if task is an event
   dispatch task to event buffer actor."
  [from [_ task]]
  (let [terms (:terms task)]
    (if (every? term-exists? terms)
      (do
        (when (event? task)
          (cast! (:event-buffer @state) [:event-msg task])
          )
        (doseq [term terms]
          (when-let [{c-ref :ref} ((:elements-map @c-bag) term)]
            (cast! c-ref [:task-msg task])
            )))
      (cast! (:concept-manager @state) [:create-concept-msg task])
      ))
  )

(def display (atom '()))
(def search (atom ""))

(defn initialise
  "Initialises actor:
    registers actor and sets actor state"
  [aname actor-ref]
  (reset! display '())
  (register! aname actor-ref)
  (set-state! {:concept-manager (whereis :concept-manager)
               :event-buffer    (whereis :event-buffer)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger search display message)
  (case type
    :task-msg (task-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def s (reify Server
         (init [_] (initialise aname @self))
         (terminate [_ cause] #_(info (str aname " terminated.")))
         (handle-cast [_ from id message] (msg-handler from message))))

(defn task-dispatcher []
  (gen-server s))