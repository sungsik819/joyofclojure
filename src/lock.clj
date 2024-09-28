(ns lock
  (:refer-clojure :exclude [agent aset count seq])
  (:require [clojure.core :as clj]
            [thread-util :refer [dothreads!]]))

(defprotocol SafeArray
  (aset [this i f])
  (aget [this i])
  (count [this])
  (seq [this]))

(defn make-dumb-array [t sz]
  (let [a (make-array t sz)]
    (reify
      SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i] (clj/aget a i))
      (aset [this i f]
        (clj/aset a
                  i
                  (f (aget this i)))))))

(defn pummel [a]
  (dothreads! #(dotimes [i (count a)] (aset a i inc))
              :threads 100))

(def D (make-dumb-array Integer/TYPE 8))

(pummel D)

;; 전부 100으로 표시되어야 하는데 표시되지 않는다.
(seq D)

;; 락을 사용한 안전한 변경
(defn make-safe-array [t sz]
  (let [a (make-array t sz)]
    (reify
      SafeArray
      (count [_] (clj/count a))
      (seq [_] (clj/seq a))
      (aget [_ i]
        (locking a
          (clj/aget a i)))
      (aset [this i f]
        (locking a
          (clj/aset a
                    i
                    (f (aget this i))))))))

(def A (make-safe-array Integer/TYPE 8))
(pummel A)
(seq A)

;; 자바의 명시적 락 사용

