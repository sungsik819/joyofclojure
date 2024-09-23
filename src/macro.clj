(ns macro
  (:require [clojure.walk :as walk]))

;; 매크로 이해
;; 리드 타임, 매크로 익스팬션 타임, 컴파일 타임, 런 타임
(->
 25
 Math/sqrt
 int
 list)

(->
 (/ 144 12)
 (/ 2 3)
 str
 keyword
 list)

(->
 (/ 144 12)
 (* 4 (/ 2 3))
 str
 keyword
 (list :33))

;; 데이터는 코드가 데이터다.
;; 문자적 구문과 프로그램의 실제 구문과 차이가 없다.
(eval 42)
(eval '(list 1 2))

;; 1을 함수로 취급 하기 때문에 오류가 발생 한다.
(eval (list 1 2))

;; symbol 함수가 문자열 +를 받아서 + 의 심벌 데이터 타입을 리턴
;; list 함수가 세개의 인자를 받아서 리스트를 리턴
;; eval 함수가 리스트 데이터 타입을 받아서 정수 3리턴
(eval (list (symbol "+") 1 2))

(->
 (symbol "+")
 (list 1 2)
 eval)

;; 문법 인용(`), 비인용(~), 평가 이음 기호(~@)
;; `'~v 패턴을 사용해서 런타임에 바인딩 되는 값을 모아둔다.
(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)] ;; 컴파일타임에 let 바인딩 생성
      ~expr)))

(contextual-eval '{a 1 b 2} '(+ a b))
(contextual-eval '{a 1 b 2} '(let [b 1000] (+ a b)))


;; 중첩된 문법 인용 활용
(let [x 9 y '(- x)]
  (println `y)
  (println ``y)
  (println ``~y)
  (println ``~~y)
  (contextual-eval {'x 36} ``~~y))

;; 매크로 사용의 경험적 규칙
;; 함수로도 가능한 기능이면 매크로를 작성하지 않는다. 매크로는 문법적 추상화나 바인딩 구문 생성이 필요할 때 작성하자
;; 매크로의 예제를 작성해두자
;; 작성한 매크로의 예제를 직접 확장해보자
;; macroexpand, pacroexpand-1 clojure.walk.macroexpand-all 등을 사용해서 구현한 매크로의 동작을 확인하자
;; 작성한 매크로를 REPL에서 테스트해보자
;; 복잡한 매크로는 가능하면 작은 함수들로 분리 하자

;; 제어 구조 정의
;; 구문을 컴파일 타임에 처리하고, 런타임 구문으로 변환 할 수 있다.

;; 문법 인용 없이 제어구조 정의하기
(defmacro do-until [& clauses]
  (when clauses
    (list 'clojure.core/when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException.
                    "do-until requires an even number of forms")))
          (cons 'do-until (nnext clauses)))))

(do-until
 (even? 2) (println "Even")
 (odd? 3) (println "Odd")
 (zero? 1) (println "You never see me")
 :lollipop (println "Truthy thing"))

;; 매크로 전개
(macroexpand-1 '(do-until true (prn 1) false (prn 2)))


;; 매크로로 정의된 모든 항목들을 전개 한다.
(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))

(do-until true (prn 1) false (prn 2))

;; 문법 인용과 비인용을 사용하여 제어 구조 정의
;; unless 구현
;; 평가 이음 기호는 값을 전개 하여 실행 한다.
(defmacro unless [condition & body]
  `(if (not ~condition) ;; 비인용
     (do ~@body))) ;; 평가 이음 기호

(unless (even? 3) "Now we se it...")
(unless (even? 2) "Now we don't.")

(unless true (println "nope"))
(unless false (println "yep!"))

;; 비인용이 필요한 이유에 대한 예시
;; condition이 비인용이 아니므로 
;; macro/condition 을 칮는다.
(macroexpand `(if (not condition) "got it"))

;; 비인용이 아니므로 condition은 eval시 오류 발생
;; No such var: macro/condition
(eval `(if (not condition) "got it"))

;; 정의 후 실행 하면 문제 없이 동작 됨
(def condition false)
(eval `(if (not condition) "got it"))

;; 구문 결합 매크로
;; defn 매크로는 아래 흐름을 단순화 한다.
;; 1. fn을 사용하여 대상이 되는 함수 객체 생성
;; 2. 도큐먼트 문자열을 확인하여 추가
;; 3. :arglists 메타데이터 구성
;; 4. 함수 이름을 var에 바인딩
;; 5. 메타데이터 추가
;; defn을  사용하면 위 과정을 한번에 해결 할 수 있다.

;; 클로저의 매크로는 언어 사용자가 언어의 구조 안에 있는 문제 영역에 빠지게 만드는 것이 아니다.
;; 언어 자체를 문제 영역에 녹아들게 해준다. 이를 도메인 특화 언어(DSL)라고 부른다.
;; 리습은 투명성의 관점에서의 DSL과 API의 차이가 미미하다.

;; def-watched 연습
;; def-watched는 아래 흐름을 단순화 한다.
;; 1. var 정의
;; 2. 관찰자가 될 함수 정의(또는 인라인으로 구현하는 것도 가능)
;; 3. 적절한 값을 입력하여 add-watch 함수 호출

(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name)
                :re-bind
                (fn [~'key ~'r old# new#]
                  (println old# " -> " new#)))))

(def x 0)
(def-watched x (* 12 12))