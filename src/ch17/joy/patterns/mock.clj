(ns joy.patterns.mock
  (:require [joy.patterns.abstract-factory :as factory]
            [joy.patterns.di :as di]))

;; 시스템에 대한 목(mock) 만들기

(defrecord MockSim [name])

(def starts (atom 0))

;; 목 시스템을 기존의 프로토콜로 확장하기
(extend-type MockSim
  di/Sys
  (start! [this]
    (if (= 1 (swap! starts inc))
      (println "Started a mock simulator.")
      (throw (RuntimeException. "Called start! more than once."))))
  (stop! [this] (println "Stopped a mock simulator."))

  di/Sim
  (handle [_ _] 42))

;; build-system 함수를 확장하는 대신 construct 멀티메서드를 확장 하자
;; 목 시스템을 위한 생성자
(defmethod factory/construct [:mock nil]
  [nom _]
  (MockSim. nom))