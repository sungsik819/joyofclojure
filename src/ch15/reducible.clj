(ns reducible
  (:require [criterium.core :as crit]
            [clojure.core.reducers :as r]
            [clojure.core.protocols :as protos]))

;; Reducible

;; 축약 가능한 컬렉션

;; lazy-seq를 활용한 클로저 range 함수 재구현
(defn empty-range? [start end step]
  (or (and (pos? step) (>= start end))
      (and (neg? step) (<= start end))))

(defn lazy-range [i end step]
  (lazy-seq
   (if (empty-range? i end step)
     nil
     (cons i
           (lazy-range (+ i step)
                       end
                       step)))))

;; 축약 함수를 리턴하도록 range 함수 재구현 하기
(defn reducible-range [start end step]
  (fn [reducing-fn unit]
    (loop [result unit i start]
      (if (empty-range? i end step)
        result
        (recur (reducing-fn result i)
               (+ i step))))))

;; 첫 번째 축약 함수 반환기
(defn half [x]
  (/ x 2))

(half 4)

(half 7)

;; reduce 관점에서 half를 사용하기 위한
;; sum-half 만들기
(defn sum-half [result input]
  (+ result (half input)))

;; 하드코딩된 + 를 매개 변수로 대체
(defn half-transformer [f1]
  (fn f1-half [result input]
    (f1 result (half input))))

;; half도 축약 하는 함수 생성
;; 여기서도 fn이 한번 더 사용하여 half도 숨긴다.

;; Reducible과 함께 사용되는 mapping의 핵심 부분
;; 변환기가 아닌 변환기 생성자 이다.
;; map-transformer가 변환기에 해당 된다.
(defn mapping [map-fn]
  (fn map-transformer [f1]
    (fn [result input]
      (f1 result (map-fn input)))))

;; 더 나은 축약 함수 변환기들
;; Reducible과 함께 사용되는 filtering의 핵심 부분
(defn filtering [filter-pred]
  (fn [f1]
    (fn [result input]
      (if (filter-pred input)
        (f1 result input)
        result))))

;; Reducible과 함꼐 사용되는 mapcatting의 핵심 부분
(defn mapcatting [map-fn]
  (fn [f1]
    (fn [result input]
      (let [reducible (map-fn input)]
        (reducible f1 result)))))

;; reducible-range로 부터 숫자를 받아 새 reducible을 리턴하는 보조 함수
(defn and-plus-ten [x]
  (reducible-range x (+ 11 x) 10))

;; reducible 변환기
;; 아래 로직을 reducible로 변환
;; (filter #(not= % 2)
;;        (map half
;;              (lazy-range 0 10 2)))

;; 두가지 reducible 변환기
(defn r-map [mapping-fn reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((mapping mapping-fn) reducing-fn) init)))

(defn r-filter [filter-pred reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((filtering filter-pred) reducing-fn) init)))

(def our-final-reducible
  (r-filter #(not= % 2)
            (r-map half
                   (reducible-range 0 10 2))))

;; reducible의 단점 
;; 지연을 포기 해야 한다.
;; 지연 시퀀스를 다룰 때처럼 연산 지연을 위해 스레드나 컨티뉴에이션을 사용 할 수 없다.

;; 클로저 reducer로 reducible 통합하기
(defn core-r-map [mapping-fn core-reducible]
  (r/reducer core-reducible (mapping mapping-fn)))

(defn core-r-filter [filter-pred core-reducible]
  (r/reducer core-reducible (filtering filter-pred)))

;; CollReduce 프로토콜을 통해 reducible 범위 구현 하기
(defn reduce-range [reducing-fn init start end step]
  (loop [result init i start]
    (if (empty-range? i end step)
      result
      (recur (reducing-fn result i)
             (+ i step)))))

(defn core-reducible-range [start end step]
  (reify protos/CollReduce
    (coll-reduce [this reducing-fn init]
      (reduce-range reducing-fn init start end step))
    (coll-reduce [this reducing-fn]
      (if (empty-range? start end step)
        (reducing-fn)
        (reduce-range reducing-fn start (+ start step) end step)))))


;; fold 함수 : 병렬로 축약하기
(reduce + [1 2 3 4 5])
(r/fold + [1 2 3 4 5])

(defn core-f-map [mapping-fn core-reducible]
  (r/folder core-reducible (mapping mapping-fn)))

(defn core-f-filter [filter-pred core-reducible]
  (r/folder core-reducible (filtering filter-pred)))

(r/fold +
        (core-f-filter #(not= % 2)
                       (core-f-map half
                                   [0 2 4 6 8])))

;; 이미 클로저에서 함수를 제공 하고 있음
(r/fold +
        (r/filter #(not= % 2)
                  (r/map half
                         [0 2 4 6 8])))

;; 초기 값을 받지 않는 대신 초기 값 결정을 위해 인자가 없는 결합 함수를 호출 한다.
;; 아럐 예에서는 초기 값이 100이다.
(r/fold (fn ([] 100) ([a b] (+ a b))) (range 10))

;; monoid를 사용해서 위 예제를 바꿀 수 있다.
(r/fold (r/monoid + (constantly 100)) (range 10))

;; 결합 함수
(r/fold 512
        (r/monoid + (constantly 100))
        +
        (range 10))

(r/fold 4
        (r/monoid conj (constantly []))
        conj
        (vec (range 10)))

(r/fold 4
        (r/monoid into (constantly []))
        conj
        (vec (range 10)))

(r/foldcat (r/filter even? (vec (range 1000))))

(seq (r/foldcat (r/filter even? (vec (range 10)))))

;; fold의 성능 확인
(def big-vector (vec (range 0 (* 10 1000 1000) 2)))

(comment
  (lazy-range 5 10 2)

  (lazy-range 6 0 -1)

  (reduce conj [] (lazy-range 6 0 -1))

  (reduce + 0 (lazy-range 6 0 -1))

  (def countdown-reducible (reducible-range 6 0 -1))

  (countdown-reducible conj [])

  (countdown-reducible + 0)

  (reduce sum-half 0 (lazy-range 0 10 2))

  ((reducible-range 0 10 2) sum-half 0)

  ;; 축약 함수 변환기
  ((reducible-range 0 10 2) (half-transformer +) 0)
  ((reducible-range 0 10 2) (half-transformer conj) [])

  ((reducible-range 0 10 2) ((mapping half) +) 0)
  ((reducible-range 0 10 2) ((mapping half) conj) [])
  ((reducible-range 0 10 2) ((mapping list) conj) [])

  ;; filtering 사용
  ((reducible-range 0 10 2) ((filtering #(not= % 2)) +) 0)
  ((reducible-range 0 10 2) ((filtering #(not= % 2)) conj) [])

  ;; 같이 사용도 가능하다.
  ;; 필터링 후 맵핑
  ((reducible-range 0 10 2)
   ((filtering #(not= % 2))
    ((mapping half) conj))
   [])

  ;; 맵핑 후 필터링
  ((reducible-range 0 10 2)
   ((mapping half)
    ((filtering #(not= % 2)) conj))
   [])

  ((and-plus-ten 5) conj [])

  ;; mapcatting 사용
  ((reducible-range 0 10 2) ((mapcatting and-plus-ten) conj) [])

  (our-final-reducible conj [])

  ;; 라이브러리를 이용한 함수의 벤치마크 측정
;; lazy-range 측정 결과
; Evaluation count : 120 in 60 samples of 2 calls.
;              Execution time mean : 521.572932 ms
;     Execution time std-deviation : 5.014426 ms
;    Execution time lower quantile : 515.978207 ms ( 2.5%)
;    Execution time upper quantile : 534.377117 ms (97.5%)
;                    Overhead used : 1.983513 ns
; 
; Found 2 outliers in 60 samples (3.3333 %)
; 	low-severe	 1 (1.6667 %)
; 	low-mild	 1 (1.6667 %)
;  Variance from outliers : 1.6389 % Variance is slightly inflated by outliers
  (crit/bench
   (reduce + 0
           (filter even?
                   (map half (lazy-range 0 (* 10 1000 1000) 2)))))

;; range로 사용
; Evaluation count : 540 in 60 samples of 9 calls.
;              Execution time mean : 123.234161 ms
;     Execution time std-deviation : 1.087397 ms
;    Execution time lower quantile : 122.238956 ms ( 2.5%)
;    Execution time upper quantile : 126.207929 ms (97.5%)
;                    Overhead used : 1.983513 ns
; 
; Found 5 outliers in 60 samples (8.3333 %)
; 	low-severe	 1 (1.6667 %)
; 	low-mild	 4 (6.6667 %)
;  Variance from outliers : 1.6389 % Variance is slightly inflated by outliers
  (crit/bench
   (reduce + 0
           (filter even?
                   (map half (range 0 (* 10 1000 1000) 2)))))

;; reducible 사용 - range가 더 빠른거 같네
; Evaluation count : 480 in 60 samples of 8 calls.
;              Execution time mean : 132.052383 ms
;     Execution time std-deviation : 4.340920 ms
;    Execution time lower quantile : 126.754512 ms ( 2.5%)
;    Execution time upper quantile : 141.341434 ms (97.5%)
;                    Overhead used : 1.983513 ns
; 
; Found 2 outliers in 60 samples (3.3333 %)
; 	low-severe	 2 (3.3333 %)
;  Variance from outliers : 19.0360 % Variance is moderately inflated by outliers
  (crit/bench
   ((r-filter even? (r-map half
                           (reducible-range 0 (* 10 1000 1000) 2))) + 0))

  ;; 위 정의한 함수는 아래와 같이 사용 한다.
  ;; 위에서 정의한 core-r-filter, core-r-map을 사용
  (reduce conj []
          (core-r-filter #(not= % 2)
                         (core-r-map half [0 2 4 6 8])))

  ;; CollReduce로 구현된 코드
  (reduce conj []
          (core-r-filter #(not= % 2)
                         (core-r-map half
                                     (core-reducible-range 0 10 2))))

  (reduce + (core-reducible-range 10 12 1))
  (reduce + (core-reducible-range 10 11 1))
  (reduce + (core-reducible-range 10 10 1))

  ;; fold 벤치마크 확인
  ; Evaluation count : 3960 in 60 samples of 66 calls.
;              Execution time mean : 15.844225 ms
;     Execution time std-deviation : 653.202242 µs
;    Execution time lower quantile : 15.357126 ms ( 2.5%)
;    Execution time upper quantile : 17.717473 ms (97.5%)
;                    Overhead used : 1.983513 ns
; 
; Found 11 outliers in 60 samples (18.3333 %)
; 	low-severe	 3 (5.0000 %)
; 	low-mild	 8 (13.3333 %)
;  Variance from outliers : 27.1033 % Variance is moderately inflated by outliers
  nil
  (crit/quick-bench
   (r/fold + (core-f-filter even? (core-f-map half big-vector)))))


