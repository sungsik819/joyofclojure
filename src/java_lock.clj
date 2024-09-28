(ns java-lock
  (:refer-clojure :exclude [agent aset count seq])
  (:require [clojure.core :as clj]
            [lock :refer [pummel]]) ;; protocol의 함수는 따로 import 해야한다.
  (:import [java.util.concurrent.locks ReentrantLock]
           [lock SafeArray]))

(defn lock-i [target-index num-locks]
  (mod target-index num-locks))

(defn make-smart-array [t sz]
  (let [a (make-array t sz)
        Lsz (/ sz 2)
        L (into-array (take Lsz
                            (repeatedly #(ReentrantLock.))))]
    (reify
      SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i]
        (let [lk (clj/aget L (lock-i (inc i) Lsz))]
          (.lock lk)
          (try
            (clj/aget a i)
            (finally (.unlock lk)))))
      (aset [this i f]
        (let [lk (clj/aget L (lock-i (inc i) Lsz))]
          (.lock lk)
          (try
            (clj/aset a
                      i
                      (f (.aget this i)))
            (finally (.unlock lk))))))))

(def S (make-smart-array Integer/TYPE 8))
(pummel S)
(.seq S)
