(ns core-logic
  (:require [clojure.core.logic :as logic]))

;; core.logic 소개
(logic/run* [answer]
            (logic/== answer 5))

;; 여러개의 바인딩
(logic/run* [val1 val2]
            (logic/== {:a val1 :b 2}
                      {:a 1 :b val2}))

(logic/run* [x y]
            (logic/== x y))

;; 단일화 실패
(logic/run* [q]
            (logic/== q 1)
            (logic/== q 2))

;; 여러개의 세게예서 항들을 단일화
(logic/run* [george]
            (logic/conde
             [(logic/== george :born)]
             [(logic/== george :unborn)]))

