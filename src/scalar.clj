(ns scalar)

;; 정밀도 이해하기
;; 절삭
;; M을 붙이면 전체를 표시해준다. -> java.math.BigDecimal
(let [imadeuapi 3.14259265358979323846264338327950288419716939937M]
  (println (class imadeuapi))
  imadeuapi)

;; 절삭 된다. -> java.lang.Double
(let [imadeuapi 3.14259265358979323846264338327950288419716939937]
  (println (class imadeuapi))
  imadeuapi)

;; 승급
;; 자동으로 자료형이 변경 된다.
(def clueless 9)

(class clueless) ;; => java.lang.Long
(class (+ clueless 900000000000000000)) ;; => java.lang.Long
(class (+ clueless 90000000000000000000000)) ;; => clojure.lang.BigInt
(class (+ clueless 9.0)) ;; java.lang.Double

;; 오버플로우
;; 기본타입에서 예외가 발생한다.
(+ Long/MAX_VALUE Long/MAX_VALUE)

;; unchecked 함수는 오버플로우가 필요한 상황에서만 사용한다.
;; 예외를 발생시키지 않기 때문이다.
;; (unchecked-add (Long/MAX_VALUE) (Long/MAX_VALUE))

;; 언더플로우
;; 부동 소수점에서만 발생 함
(+ 0.1M 0.1M 0.1M 0.1 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M)

(float 0.000000000000000000000000000000000000000) ;; => 0.0
1.0E-430 ;; => 0.0

;; 반올림 오류
;; 연산 내에서 double, float을 같이 사용하면 발생
(let [apporx-interval (/ 209715 2097152)
      actual-interval (/ 1 10)
      hours (* 3600 100 10)
      actual-total (double (* hours actual-interval))
      approx-total (double (* hours apporx-interval))]
  (- actual-total approx-total))

;; 유리수
;; float, double 처럼 계산이 빠르진 않다.
;; 계산에 대한 정밀도와 오류를 방지
;; 소수의 연산은 오동작 할 수 있기 때문에 유리수를 사용
1.0E-430000000M ;; => 계산 됨
;; 1.0E-4300000000M => 계산 안됨

;; 부동 소수 계산은 결합 법칙이나 분배 법칙이 성립하지 않는다.
;; 결합 법칙에 의하면 두 계산 모두 17이 되어야 한다.
(def a 1.0e50)
(def b -1.0e50)
(def c 17.0e00)

(+ (+ a b) c) ;; => 17.0
(+ a (+ b c)) ;; => 0.0

;; 유리수 만들기
;; 유리수를 만들어 계산하면 결합 법칙이 성립 한다.
(def a1 (rationalize 1.0e50))
(def b1 (rationalize -1.0e50))
(def c1 (rationalize 17.0e00))
(+ (+ a1 b1) c1)
(+ a1 (+ b1 c1))

;; 유리수 추출
;; 윗수
(numerator (/ 123 10))

;; 아래수
(denominator (/ 123 10))