(ns nal.test.deriver.rules
  (:require [clojure.test :refer :all]
            [nal.deriver.rules :refer :all]))

(deftest test-options
  (is (= {:pre [] :post []} (options [:pre [] :post []])))
  (is (= {:pre []} (options [:pre [] []]))))

(deftest test-get-conclusions
  (is (= [{:conclusion '(--> A B), :post nil}] (get-conclusions '(--> A B) {})))
  (is (= [{:conclusion '(--> A B), :post [:something]}]
         (get-conclusions '(--> A B) {:post [:something]})))
  (is (= '({:conclusion (=|> S P), :post (:t/induction :linkage-temporal)}
            {:conclusion (=|> P S), :post (:t/induction :linkage-temporal)}
            {:conclusion (<|> S P), :post (:t/comparison :linkage-temporal)}
            {:conclusion (&| S P), :post (:t/intersection :linkage-temporal)})
         (get-conclusions
           '((=|> S P) :post (:t/induction :linkage-temporal)
              (=|> P S) :post (:t/induction :linkage-temporal)
              (<|> S P) :post (:t/comparison :linkage-temporal)
              (&| S P) :post (:t/intersection :linkage-temporal)) {}))))

(def trule (rule '#R[(D =/> R) (D --> K) |- ((K =/> R) :post (:t/abduction)
                                              (R --> K) :post (:t/induction)
                                              (K </> R) :post (:t/comparison))
                     :pre ((:!= R K))]))
(deftest test-rule
  (is (= '[{:p1          (--> S P),
            :p2          (<-> S P),
            :conclusions [{:conclusion (--> S P), :post (:t/struct-int :p/judgment)}],
            :full-path   [(--> :any :any) :and (<-> :any :any)],
            :pre         (:question?)}]
         (rule '#R[(S --> P) (S <-> P) |- (S --> P) :post (:t/struct-int :p/judgment) :pre (:question?)])))
  (is (= '({:conclusions [{:conclusion (pred-impl K R)
                           :post       (:t/abduction)}]
            :full-path   [(pred-impl :any :any) :and (--> :any :any)]
            :p1          (pred-impl D R)
            :p2          (--> D K)
            :pre         ((:!= R K))}
            {:conclusions [{:conclusion (--> R K)
                            :post       (:t/induction)}]
             :full-path   [(pred-impl :any :any) :and (--> :any :any)]
             :p1          (pred-impl D R)
             :p2          (--> D K)
             :pre         ((:!= R K))}
            {:conclusions [{:conclusion (</> K R)
                            :post       (:t/comparison)}]
             :full-path   [(pred-impl :any :any) :and (--> :any :any)]
             :p1          (pred-impl D R)
             :p2          (--> D K)
             :pre         ((:!= R K))})
         trule)))