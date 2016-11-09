(ns future-sample.core
  (:import [java.util.concurrent Callable Executors ExecutorService Future]))


;; ex 1
(defn worker []
  (let [result (reduce + (range 1 100))]
    (println (.getName (Thread/currentThread)) ":" result)))

(defn simple-thread []
  (let [tasks (map (fn [_] (Thread. worker)) (range 10))]
    (doseq [t tasks]
      (.start t))
    (doseq [t tasks]
      (.join t))))

;; ex 2
(def ^:dynamic sum 0)

(defn worker2 []
  (let [result (reduce + (range 1 100))]
    (alter-var-root #'sum (fn [v] (+ v result)))))

(defn simple-thread2 []
  (let [tasks (map (fn [_] (Thread. worker2)) (range 10))]
    (doseq [t tasks]
      (.start t))
    (doseq [t tasks]
      (.join t))
    (println sum)))

;; ex 3
(def ^:dynamic counter 0)

(defn worker3 []
  (dotimes [_ 10]
    (alter-var-root #'counter inc)))

(defn simple-thread3 []
  (let [tasks (map (fn [_] (Thread. worker3)) (range 10))]
    (doseq [t tasks]
      (.start t))
    (doseq [t tasks]
      (.join t))
    (println counter)))

;; ex 4

; future를 이용해서 결과 취합
