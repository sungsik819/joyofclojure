(ns joy.patterns.app
  (:require [joy.patterns.di :as di]))

;; 환경설정으로 시스템 구성하기
(def config {:type :mock :lib 'joy.patterns.mock})

;; 의존성 주입
(defn initialize [name cfg]
  (let [lib (:lib cfg)]
    (require lib)
    (di/build-system name cfg)))

(di/handle (initialize :mock-sim config) {})

(initialize :mock-sim config)