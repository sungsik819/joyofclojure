(ns joy.patterns.di
  (:require [joy.patterns.abstract-factory :as factory]))

;; 클로저에서의 의존성 주입
(def lofi {:type :sim :descr "Lowfi sim" :fidelity :low})
(def hifi {:type :sim :descr "Hifi sim" :fidelity :high :threads 2})

(comment
  (factory/construct :lofi lofi))

;; 시스템 동작 시뮬레이션을 기술하는 프로토콜
(defprotocol Sys
  (start! [sys])
  (stop! [sys]))

(defprotocol Sim
  (handle [sim msg]))

(defn build-system [name config]
  (let [sys (factory/construct name config)]
    (start! sys)
    sys))

(extend-type joy.patterns.abstract_factory.LowFiSim
  Sys
  (start! [this]
    (println "Started a lofi simulator."))
  (stop! [this]
    (println "Stopped a lofi simulator."))

  Sim
  (handle [this msg]
    (* (:weight msg) 3.14)))



(comment
  (start! (factory/construct :lofi lofi))
  ;; => Started a lofi simulator.

  (build-system :sim1 lofi)
  ;; Started a lofi simulator.
  ;; {:name :sim1}

  (handle (build-system :sim1 lofi) {:weight 42})
  ;; Started a lofi simulator.
  ;; 131.88
  )

(extend-type joy.patterns.abstract_factory.HiFiSim
  Sys
  (start! [this] (println "Started a hifi simulator."))
  (stop! [this] (println "Stopped a hifi simulator."))

  Sim
  (handle [this msg]
    (Thread/sleep 5000)
    (* (:weight msg) 3.1415926535)))

(build-system :sim2 hifi)
(handle (build-system :sim2 hifi) {:weight 42})

;; 고성능, 저성능 모델의 답변을 모두 계산
(def excellent (promise))

(defn simulate [answer fast slow opts]
  (future (deliver answer (handle slow opts)))
  (handle fast opts))

(comment
  (simulate excellent
            (build-system :sim1 lofi)
            (build-system :sim2 hifi)
            {:weight 42})

  (realized? excellent)
  @excellent)
