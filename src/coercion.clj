(ns coercion)

;; 강제 변환 이해하기

;; 타입 선언을 사용하지 않는 꼬리 재귀 팩토리얼
(defn factorial-a [original-x]
  (loop [x original-x acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(factorial-a 10)


;; 24 밀리세컨드가 나온다.. 빠르네..
(time (dotimes [_ 1e5] (factorial-a 20)))

;; 기본형 long 사용하기

;; 로컬을 강제로 변환하여 사용하는 팩토리얼
(defn factorial-b [original-x]
  (loop [x (long original-x) acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

;; 12 밀리세컨드가 나온다..
(time (dotimes [_ 1e5] (factorial-b 20)))

;; 기본형 long 인자를 사용하는 팩토리얼 함수
(defn factorial-c [^long original-x]
  (loop [x original-x acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(time (dotimes [_ 1e5] (factorial-c 20)))

;; 오버플로우 확인 하지 않는 팩토리얼
(set! *unchecked-math* true)

(defn factorial-d [^long original-x]
  (loop [x original-x acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(time (dotimes [_ 1e5] (factorial-d 20)))

(set! *unchecked-math* false)

;; 기본형 double 사용하기

;; 기본형 double 인자를 사용한 팩토리얼
(defn factorial-e [^double original-x]
  (loop [x original-x acc 1.0]
    (if (>= 1.0 x)
      acc
      (recur (dec x) (* x acc)))))

(factorial-e 10.0)

(factorial-e 20.0)

(factorial-e 30.0)

(factorial-e 171.0)

(time (dotimes [_ 1e5] (factorial-e 20.0)))

;; 자동 승급 사용하기

;; 자동 승급을 사용하는 팩토리얼
(defn factorial-f [^long original-x]
  (loop [x original-x acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (*' x acc))))) ;; 자동 승급 곱셈 사용

(factorial-f 20)

(factorial-f 30)

(factorial-f 171)

(time (dotimes [_ 1e5] (factorial-f 20)))

;; 숫자는 기본형이나 박싱된 형태로 표현될수 있다.
;; 기본형은 오버플로우가 발생하기 쉽지만 성능이 좋다.
;; 오버플로우 확인을 하지 않는 다소 위험한 방법으로 사용 할 수도 있다.
;; 박싱된 숫자는 느리지만 자동 승급 수학 연산들을 사용하여 임의의 정밀도를 유지해준다.
;; 상황에 따라 동작과 성능 간의 트레이드오프 관계를 고려하여 적절한 타입을 선택하여 사용할 수 있다.

;; m1에서 위 코드들을 실행 했을때 최적화가 안되어도 24ms이고 최적화 하면 최대 7ms 이다.