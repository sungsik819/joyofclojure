(ns data
  (:require [joy.unit :refer [convert]]))

;; 데이터 지향 프로그래밍
(rand-int 1024)

(+ (rand-int 100) (rand-int 100))

(use 'clojure.data)

(diff [1 2 3] [1 2 4])

;; 값은 디버깅
(defn filter-rising [segments]
  (clojure.set/select
   (fn [{:keys [p1 p2]}]
     (> 0
        (/ (- (p2 0) (p1 0))
           (- (p2 1) (p1 1)))))
   segments))

(filter-rising #{{:p1 [0 0] :p2 [1 1]}
                 {:p1 [4 15] :p2 [3 21]}})

;; 태그 리터럴
#inst "2024-10-02"

;; 소스 코드 리터럴을 태그 데이터로 정의하기
(def distance-reader
  (partial convert
           {:m 1
            :km 1000
            :cm 1/100
            :mm [1/10 :cm]}))

;; 이것을 사용하기 위해서는 
;; data_readers.clj를 생성하여 정의 한다.
#unit/length [1 :km]

;; 동적 데이터 리더로 사용
(def time-reader
  (partial convert
           {:sec 1
            :min 60
            :hr [60 :min]
            :day [24 :hr]}))

(binding [*data-readers* {'unit/time #'joy.unit/time-reader}]
  (read-string "#unit/time [1 :min 30 :sec]"))

;; 연관된 리더가 없는 모든 리터럴 태그
(binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
  (read-string "#nope [:doesnt-exist]"))

;; 신뢰 할수 없는 데이터를 처리할 때는 위 방법이 유용하진 않음

;; clojure.edn으ㄹ 사용하는 것을 추천
