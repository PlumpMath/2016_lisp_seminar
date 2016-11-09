(ns seven-ch3.core
  (:require [clojure.core.protocols :refer [CollReduce coll-reduce]]
            [clojure.core.reducers :as r]))

(def numbers (into [] (range 0 10000000)))

(defn sum [numbers]
  (reduce + numbers))

(defn parallel-sum [numbers]
  (r/fold + numbers))

(defn word-frequencies [words]
  (reduce
    (fn [counts word]
      (assoc counts word (inc (get counts word 0))))
    {}
    words))

(word-frequencies ["one" "potato" "two" "potato" "three"])

(defn get-words [text]
  (re-seq #"\w+" text))

(defn count-words-sequential [pages]
  (frequencies (mapcat get-words pages)))

(defn count-words-parallel [pages]
  (reduce (partial merge-with +)
    (pmap #(frequencies (get-words %)) pages)))

(defn count-words [pages]
  (reduce (partial merge-with +)
    (pmap count-words-sequential (partition-all 100 pages))))

(defn make-reducer [reducible transformf]
  (reify
    CollReduce
    (coll-reduce [_ f1]
      (coll-reduce reducible (transformf f1) (f1)))
    (coll-reduce [_ f1 init]
      (coll-reduce reducible (transformf f1) init))))

(defn my-map [mapf reducible]
  (make-reducer reducible
    (fn [reducef]
      (fn [acc v]
        (reducef acc (mapf v))))))


