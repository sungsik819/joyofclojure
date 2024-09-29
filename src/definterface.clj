(ns definterface
  (:import [java.lang String]))

;; definterface 매크로
;; 상황에 맞게 인터페이스 생성하기

;; 조각낼 수 있는 객체를 정의하는 인터페이스
(definterface ISliceable
  (slice [^long s ^long e])
  (^int sliceCount []))

;; 더미 ISliceable 구현
(def dumb
  (reify ISliceable
    (slice [_ s e] [:empty])
    (sliceCount [_] 42)))

(.slice dumb 1 2)

(.sliceCount dumb)

;; 프로토콜을 사용한 ISliceable 확장
(defprotocol Sliceable
  (slice [this s e])
  (sliceCount [this]))

(extend ISliceable
  Sliceable
  {:slice (fn [this s e] (.slice this s e))
   :sliceCount (fn [this] (.sliceCount this))})

(sliceCount dumb)
(slice dumb 0 0)

;; String이 Sliceable 프로토콜을 따르도록 확장하기
(defn calc-slice-count [thing]
  "다음 공식을 사용하여 가능한 조각 개수를 계산:
   (n + r - 1)!
   ------------
   r!(n - 1)!
   n은 (count thing), r은 2"
  (let [! #(reduce * (take % (iterate inc 1)))
        n (count thing)]
    (/ (! (- (+ n 2) 1))
      (* (! 2) (! (- n 1))))))

(extend-type String
  Sliceable
  (slice [this s e] (.substring this s (inc e)))
  (sliceCount [this] (calc-slice-count this)))

(slice "abc" 0 1)
(sliceCount "abc")

;; defprotocol에 비교해서 definterface가 갖는 장점은
;; 인자와 리턴 타입에 기본형 타입들을 사용 할 수 있다는 것 뿐이다.
;; 꼭 필요하지 않는 이상 defprotocol을 사용하자