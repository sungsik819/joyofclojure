(ns edn
  (:require [clojure.edn :as edn]))

;; clojure.edn을 사용한 클로저의 EDN 데이터 처리
#uuid "dae78a90-d491-11e2-8b8b-0800200c9a66"

(class #uuid "dae78a90-d491-11e2-8b8b-0800200c9a66")

(edn/read-string "#uuid \"dae78a90-d491-11e2-8b8b-0800200c9a66\"")

(edn/read-string "42")

(edn/read-string "{:a 42 \"b\" 36 [:c] 9}")

;; 처리 되지 않음
(edn/read-string "#unit/time [1 :min 30 :sec]")

;; data.clj에 있는 함수들이 실행 되어야 아래 코드 실행 됨
(def T {'unit/time #'joy.unit/time-reader})
(edn/read-string {:readers T} "#unit/time [1 :min 30 :sec]")

;; 기본 리더 함수 지정
(edn/read-string {:readers T :default vector} "#what/the :huh?")






