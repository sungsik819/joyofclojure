(ns recursive)

;; 재귀적으로 생각 하기
;; 일반적 재귀
(defn pow [base exp]
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))

(pow 2 3)

(pow 1.01 925)

;; 스택오버플로우 발생
(pow 2 10000)

;; 꼬리재귀 사용
(defn pow2 [base exp]
  (letfn [(kapow [base exp acc]
            (if (zero? exp)
              acc
              (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))

(pow2 2N 10000)

;; 재귀적 단위 계산기
(def simple-metric {:meter 1
                    :km 1000
                    :cm 1/100
                    :mm [1/10 :cm]})

;; 3킬로미터, 10미터, 80센티미터, 10밀리미터가 몇 미터인가?
(-> (* 3 (:km simple-metric))
    (+ (* 10 (:meter simple-metric)))
    (+ (* 80 (:cm simple-metric)))
    (+ (* (:cm simple-metric)
          (* 10 (first (:mm simple-metric)))))
    float)

;; 위 과정을 함수로 재귀를 사용하여 만들기
(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]
            (+ result
               (let [val (get context unit)]
                 (if (vector? val)
                   (* mag (convert context val))
                   (* mag val)))))
          0
          (partition 2 descriptor)))

(convert simple-metric [1 :meter])

(convert simple-metric [50 :cm])

(convert simple-metric [100 :mm])

(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))
(partition 2 [3 :km 10 :meter 80 :cm 10 :mm])

;; 최대 공약수
;; 일반적인 재귀호출
(defn gcd [x y]
  (cond
    (> x y) (gcd (- x y) y)
    (< x y) (gcd x (- x y))
    :else x))

;; 왜 재귀인가?
;; 암묵적이 아닌 명시적으로 recur을 제공 하는 이유?
;; 자바 가상머신을 일반적으로 꼬리 호출 최적화가 존재하지 않는다.
;; 명시적으로 제공 함으로써 밖으로 밀려나는 오류를 컴파일러가 감지할 수 있다.
;; fn 구문과 loop 구문이 익명의 재귀점으로 동작 할 수 있다.
;; 아래 코드는 정의시 오류가 발생 한다.
(defn gcd2 [x y]
  (int
   (cond
     (> x y) (recur (- x y) y)
     (< x y) (recur x (- x y))
     :else x)))

;; 트램펄린
;; 상호 재귀를 위해 사용
;; 상호재귀란 A 함수가 -> B를 호출하고 B 함수가 -> A 함수를 호출 하는 상황
;; 상호재귀는 결과값이 아닌 함수를 리턴 한다.
;; trampoline을 이용하여 상호 재귀의 첫 함수를 호출 한다.
(defn elevator [commands]
  (letfn
   [(ff-open [[_ & r]]
      "엘리베이터가 1층에 문이 열려 있으면 문을 닫거나 종료 할 수 있다."
      #(case _
         :close (ff-closed r)
         :done true
         false))
    (ff-closed [[_ & r]]
      "엘리베이터가 1층에서 문이 닫혀있으면 문을 열거나 올라갈 수 있다."
      #(case _
         :open (ff-open r)
         :up (sf-closed r)
         :false))
    (sf-closed [[_ & r]]
      "엘리베이터가 2층에서 문이 닫혀있으면 문을 열거나 내려갈 수 있다."
      #(case _
         :down (ff-closed r)
         :open (sf-open r)
         false))
    (sf-open [[_ & r]]
      "엘리베이터가 2층에서 문이 열려있으면 문을 닫거나 종료할 수 있다."
      #(case _
         :close (sf-closed r)
         :done true
         false))]
    (trampoline ff-open commands)))

(elevator [:close :open :close :up :open :open :done])

(elevator [:close :up :open :close :down :open :done])

;; 컨티뉴에이션 패싱 스타일(CPS)
;; 컴퓨터 연산을 일반화 하기 위해 세가지 함수를 사용한다.
;; Accept - 연산 종료 시점 설정
;; Return - 리턴 할 값을 래핑
;; Continuation - 다음 연산 제공

;; 프로그램의 흐름을 제어하기 위해 continuation를 명시적으로 전달하는 스타일, 
;; 프로그램의 현재 상태를 나타내는 객체인 continuation을 다음 함수 호출에 전달하여 제어 흐름을 유지

;; 팩토리얼로 CPS 표현
(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))] ;; Continuation
    (if (zero? n) ;; Accept
      (k 1) ;; Return
      (recur (dec n) cont))))

(defn fac [n]
  (fac-cps n identity))

(fac 10)

;; CPS를 통해 더 일반적인 함수를 만들어 낼 수 있다.
(defn mk-cps [accept? kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v] ;; Continuation
                    (k ((partial kont v) n)))]
         (if (accept? n) ;; Accept
           (k 1) ;; Return
           (recur (dec n) cont))))
     n kend)))

;; 위 함수를 바탕으로 구현 함수 만들기
;; 팩토리얼
(def fac-mk
  (mk-cps zero?
          identity
          #(* %1 %2)))

(fac-mk 10)

;; 트라이앵글 함수
(def tri
  (mk-cps #(= 1 %)
          identity
          #(+ %1 %2)))

(tri 10)

;; 클로저에서 CPS는 널리 사용되지 않는다.




