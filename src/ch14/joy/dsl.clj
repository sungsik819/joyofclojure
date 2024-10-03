(ns joy.dsl
  (:require [joy.unit :refer [convert]]))

;; 명세 예시
;; 거리의 기본 단위는 미터다. 
;; 1000미터는 1킬로미터이고, 1미터는 100센티미터다.
;; 1센티미터는 10밀리미터다, 
;; 1미터는 3.28083피트다
;; 마지막으로 1마일은 5,280 피트다

;; 단위 변환을 구현하는 기본적인 방법
;; 위 내용을 하나하나 함수로 만드는 방법
(defn meters->feet [m] (* m 3.28083989501312))
(defn meters->miles [m] (* m 0.000621))

;; 조스위그가 명세를 괄호로 감싸기라고 불렸던 기법을 활용
;; 첫번째 단계로 클로저의 몇가지 문법적인 요소들을 사용하여 
;; 가장 명시적인 부분들을 그룹으로 묶어보자
;; (거리의 기본 단위는 :meter
;;   [1000 :meter 는 1 :kilometer]
;;   [100 :centimeter 는 1 :meter]
;;   [10 :millimeter 는 1 :centimeter]
;;   [3.28083 :feet 는 1 :meter]
;;   [5280 :feet 는 1 :meter])

;; 위 내용을 더 줄여 보자
;; (거리 단위 정의
;;   {:m 1
;;    :km 1000
;;    :cm 1/100
;;    :mm [:cm의 1/10]
;;    :ft 0.3048
;;    :mile [은 5280 :ft]})

;; convert 함수 사용하여 구현 하기
;; convert 함수에서 사용 가능한 규격인지 모르므로 한번 감싸준다.

(defn relative-units [context unit]
  (if-let [spec (get context unit)]
    (if (vector? spec)
      (convert context spec)
      spec)
    (throw (RuntimeException. (str "Undefined unit " unit)))))

;; 한글로된 구문은 제거하여 정의 해보자
;; (defunits-of distance :m
;;   :km 1000
;;   :cm 1/100
;;   :mm [1/10 :cm]
;;   :ft 0.3048
;;   :mile [5280 :ft])

;; 위 명세를 구현 할 수 있도록 defunit-of를 매크로로 만든다.
(defmacro defunits-of [name base-unit & conversions]
  (let [magnitude (gensym)
        unit (gensym)
        units-map (into `{~base-unit 1}
                        (map vec (partition 2 conversions)))]
    `(defmacro ~(symbol (str "unit-of-" name))
       [~magnitude ~unit]
       `(* ~~magnitude
           ~(case ~unit
              ~@(mapcat
                 (fn [[u# & r#]]
                   `[~u# ~(relative-units units-map u#)])
                 units-map))))))

;; 주석 처리된 명세를 코드로 변경
(defunits-of distance :m
  :km 1000
  :cm 1/100
  :mm [1/10 :cm]
  :ft 0.3048
  :mile [5280 :ft])


(comment
  (meters->feet 1609.344)
  (meters->miles 1609.344)

  (relative-units {:m 1 :cm 1/100 :mm [1/10 :cm]} :m)
  (relative-units {:m 1 :cm 1/100 :mm [1/10 :cm]} :mm)
  (relative-units {:m 1 :cm 1/100 :mm [1/10 :cm]} :ramsden-chain)

  (unit-of-distance 1 :m)
  (unit-of-distance 1 :mm)
  (unit-of-distance 1 :ft)
  (unit-of-distance 1 :mile)

  (unit-of-distance 441 :ft))