(ns joy.quote)

;; 평가
;; 컬렉션이 평가될 때는 그 안에 포함된 각 항목들을 먼저 평가 한다.
(cons 1 [2 3])

;; 로컬이나 var를 평가하면 해당 값을 리턴 한다.
cons

;; 리터럴 스칼라 값은 스스로 평가 한다.
1 ;; => 1

;; 백터도 마찬가지 이다.
[2 3] ;; => [2 3]

;; 인용
;; 심벌이 평가 되는 것을 막아준다.
(def age 9)
(quote age) ;; age로 표시되고 9로 평가되지 않는다.

;; 전체가 그대로 리턴 된다.
(quote (cons 1 [2 3]))

;; 리스트를 평가되지 않게 하기 위한 용도
;; (2 3)이 미리 평가되지 않도록 quote로 감싼다.
(cons 1 (quote (2 3)))

;; quote의 축약형
(cons 1 '(2 3))

;; 백터는 평가되나 인용을 사용한 코드는 평가되지 않는다.
'(1 (+ 2 3)) ;; => (1 (+ 2 3))
[1 (+ 2 3)] ;; => [1 5]

;; 문법 인용
;; 컬렉션이 코드로 사용될 수 있도록 만드는데 적합 하다.
`(1 2 3)

;; 문법 인용 특징
;; 심벌 자동 식별
;; 식별되지 않은 심벌들을 식별 한다.
;; 즉, 어디에 속한 심벌인지 식별 한다.
`map ;; => clojure.core/map
`Integer ;; => java.lang.Integer
`(map even? [1 2 3]) ;; => (clojure.core/map clojure.core/even? [1 2 3])

;; 기존에 존재하는 var이나 클래스가 아니라면 현재 네임스페이스를 사용 한다.
`is-always-right

;; 비인용
;; quote를 사용하는 코드 내부에서 평가되어야 하는 코드에 사용 한다.
;; 즉, 인용구문을 사용한 사용한 코드에서 일부의 코드를 평가하기 위해서 사용한다.
`(+ 10 (* 3 2)) ;; => (clojure.core/+ 10 (clojure.core/* 3 2))
`(+ 10 ~(* 3 2)) ;; => (clojure.core/+ 10 6)

`(+ 1 ~(+ 1 2))

(let [x 2]
  `(1 ~x 3))

;; (1 2 3)이 아닌 (1 (2 3))으로 표시 된다.
;; (1 (2 3))을 (1 2 3)으로 나타내기 위해서는 평가 이음 기호를 사용 한다.
(let [x '(2 3)]
  `(1 ~x))

;; 평가 이음 기호
;; 전개 하여 실행 된다 즉, 외부의 괄호를 없앤 데이터를 사용한다
;; (1 2 3)이 된다.
(let [x '(2 3)]
  `(1 ~@x))

;; 심벌 자동 생성
;; 인자나 let으로 정의한 로컬 이름 등을 위한 고유한 심벌이 필요 할 때 
`potion#

;; 설명에서와 같이 let을 사용하여 local 이름을 생성하려면 #을 추가 해야한다.
(def number 3)
;; 외부 심벌을 사용하므로 결과는 5가 된다.
(eval `(let [number# 1]
         (+ number 2)))

;; 로컬 심볼을 사용하므로 결과는 3이 된다.
(eval `(let [number# 1]
         (+ number# 2)))

;; 호스트 라이브러리 사용
java.util.Locale/JAPAN

(Math/sqrt 9)

;; 인스턴스 생성
;; new 로 생성하기 보다는
(new java.awt.Point 0 1)
(new java.util.HashMap {"foo" 42 "bar" 9 "baz" "quux"})

;; 이렇게 생성하는 것을 선호
(java.util.HashMap. {"foo" 42 "bar" 9 "baz" "quux"})

;; 인스턴스 맴버 접근
(.-x (java.awt.Point. 10 20))

(.divide (java.math.BigDecimal. "42") 2M)

;; 인스턴스 필드 세팅
(let [origin (java.awt.Point. 0 0)]
  (set! (.-x origin) 15)
  (str origin))

;; .. 매크로
;; new java.util.Date().toString().endsWith("2024")
(.endsWith (.toString (java.util.Date.)) "2024")

;; .. 예시
;; ->, ->> 를 선호 한다.
(.. (java.util.Date.) toString (endsWith "2024"))

;; doto 매크로
(doto (java.util.HashMap.)
  (.put "Home" "/home/me")
  (.put "SRC" "src")
  (.put "BIN" "classes"))

;; 예외 처리
(throw (Exception. "I done throwed"))

(defn throw-catch [f]
  [(try
     (f)
     (catch ArithmeticException e "No dividing by zero!")
     (catch Exception e (str "You are so bad " (.getMessage e)))
     (finally (println "returning...")))])

(throw-catch #(/ 10 5))

(throw-catch #(/ 10 0))
(throw-catch #(throw (Exception. "Crybaby")))

;; 네임 스페이스
;; (ns joy.ch2)

(defn hello []
  (println "Hello Cleveland!"))

(defn report-ns []
  (str "The current namespace is " *ns*))

(report-ns)

hello

;; 아무 때나 새 네임 스페이스 생성 가능
;; (ns joy.another)

;; 해당 네임스페이스에느 없기 때문에 동작하지 않는다.
;; (report-ns)