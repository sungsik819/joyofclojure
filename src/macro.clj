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

;; 매크로 사용하여 구문 변경
;; 클로저 격언 : 클로저는 그 개념적 모델 또한 클로저로 구성된 설계 언어다.
;; 매크로를 설계하는 한가지 방법은 원하는 동작에 대한 예제 코드를 작성
;; 동작시킬 구체적인 애플리케이션 도메인과 사양에 가깝게 작성 되어야 한다.
;; 그리고 나서 세부 동작을 채우기 위한 매크로와 함수를 작성하면 된다.

;; 소프트웨어 시스템을 설계 할 때 주어진 애플리케이션 도메인을 구성하는 요소들을
;; 정의해두는 것이 종종 도움이 된다.
;; 여기에는 요소들 간의 논리적 그룹도 포함 된다.
;; 이 단계에서의 설게는 그 추상화 단계를 높게 설정 해두고, 구현의 상세 내용을 포함하지 않는다.

;; 인간과 괴물 대결에 관한 도메인 예시
;; 1. 인간(man) vs 괴물(monster)
;; 1.1 인간(people)
;; 1.1.1 사람(Men / Human)
;; 1.1.1.1 이름(name)
;; 1.1.1.2 수염(beard)이 있는가?

;; 1.2 괴물(Monster)
;; 1.2.1 추파카브라(Chupacabra)
;; 1.2.1.1 염소(goat)를 잡아먹는가?

;; 컴파일 오류 피하기 

;; 일단은 함수가 있다고 가정하고 최상단 부터 작성 한다.
(comment
  (domain man-vs-monster
          (grouping people
                    (Human "A stock human")
                    (Man (isa Human)
                         "A man, baby"
                         [name]
                         [has-beard?]))
          (grouping monsters
                    (Chupacabra
                     "A fierce, yet elusive creature"
                     [eats-goats?]))))

;; 최상단 추상부분 작성후 아래를 작성 한다.
(defmacro domain [name & body]
  `{:tag :domain
    :attrs {:name (str '~name)}
    :content [~@body]})

;; 컴파일 오류 피하기
(declare handle-things)

(defmacro grouping [name & body]
  `{:tag :grouping
    :attrs {:name (str '~name)}
    :content [~@(handle-things body)]})

;; 컴파일 오류 피하기
(declare grok-attrs grok-props)

(defn handle-things [things]
  (for [t things]
    {:tag :thing
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c]
                [])}))

;; 백터가 아닌 모든 것 처리
(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

;; 속성들을 처리
(defn grok-props [props]
  (when props
    {:tag :properties
     :attrs nil
     :content (apply vector (for [p props]
                              {:tag :property
                               :attrs {:name (str (first p))}
                               :content nil}))}))

;; vscode에서 빨간색으로 표시되지만 실행되는 코드임
(def d (domain man-vs-monster
               (grouping people
                         (Human "A stock human")
                         (Man (isa Human)
                              "A man, baby"
                              [name]
                              [has-beard?]))
               (grouping monsters
                         (Chupacabra
                          "A fierce, yet elusive creature"
                          [eats-goats?]))))

(:tag d)

(:tag (first (:content d)))

;; xml로 변환 해보기
(use '[clojure.xml :as xml])
(xml/emit d)

;; 도메인에 대한 전체적인 구조는 하나의 매크로를 사용하여 기술
;; 세부 역할들은 구분한다.
;; 매크로는 항상 데이터를 받아서 데이터를 리턴 한다.

;; 매크로로 심벌릭 레졸루션 타임 제어하기
;; 함수는 런 타임에 값을 받아서 애플리케이션이 필요하는 값을 리턴
;; 매크로는 컴파일 타임에 코드를 받아서 필요한 코드를 리턴
;; 심벌 식별 여부, 레졸루션 타임, 렉시컬 컨텍스트에 따라서 미묘한 차이가 있음
;; 네임 캡처 - 매크로 시스템에서 컴파일 타임에 생성된 이름이 런 타임에 존재하는 
;; 이름과 충돌할 수 있는 잠재적 문제

;; 클로저 매크로는 문법 인용의 사용으로 인해 매크로 익스팬션 타임에 심벌들을 식별하여
;; 네임 캡처 문제가 발생하지 않는다.

(defmacro resolution [] `x)

(macroexpand '(resolution)) ;; => #'macro/x

;; 아래 코드는 x가 문법 인용이 적용된 코드 이므로 9가 출력 된다.
(def x 9)
(let [x 109] (resolution))

;; 대용
;; 대명사와 같은 뜻, 어떤 것을 대체 한다.
(defmacro awhen [expr & body]
  `(let [~'it ~expr] ;; 대용어 정의
     (if ~'it ;; 참인지 확인
       (do ~@body)))) ;; body 인라인 평가

;; ([1 2 3] 2)과 같은 동작
;; (clojure.core/let [it [1 2 3]] (if it (do (it 2))))
;; it 사용
(awhen [1 2 3] (it 2))

(awhen nil (println "Will never get here"))

(awhen 1 (awhen 2 [it]))

;; 중첩 실패
(macroexpand-1 '(awhen 1 (awhen 2 [it])))

;; 중첩이 가능하면서도 대용어의 필요를 대체 할 수 있는
;; if-let, when-let 매크로 제공

;; 선택적 네임 캡처
;; 하이제닉
;; 클로저 매크로 안에서의 이름을 선택적으로 캡처하고 싶다면 ~' 패턴을 사용하여 명시 한다.

;; 매크로로 리소스 관리 하기
;; with-open 매크로는 자동적으로 finally 블록에서 .close를 호출 해준다.
(import [java.io BufferedReader InputStreamReader]
        [java.net URL])

;; 닫은 후에 io 시도
(defn joc-www []
  (-> "https://joyofclojure.github.io/" URL.
      .openStream
      InputStreamReader.
      BufferedReader.))

(let [stream (joc-www)]
  (with-open [page stream] ;; io 블록 시작
    (println (.readLine page))
    (print "The stream will now close... ")) ;; 암묵적으로 io를 닫음
  (println "but let's read from it anyway.")
  (.readLine stream)) ;; 리소스 해제 후의 부적절한 io 시도

;; 모든 리소스의 인스턴스를 닫을 수 있는 것은 아니다.
;; 아래 예제는 리소스 할당을 위한 매크로
(defmacro with-resource [binding close-fn & body]
  `(let ~binding
     (try
       (do ~@body)
       (finally
         (~close-fn ~(binding 0))))))

(let [stream (joc-www)]
  (with-resource [page stream]
    #(.close %)
    (.readLine page)))

;; 함수를 리턴하는 매크로
;; 클로저 격언 : 클로저 프로그래머는 클로저로 애플리케이션을 작성하는 것이 아니라,
;; 클로저로 애플리케이션을 작성하는 데 사용되는 언어를 작성하는 것이다.

;; contract 매크로
;; 함수의 제약 조건을 기술하는 간단한 DSL
;; 1. 이름을 부여 할 수 있어야 한다.
;; 2. 선행/후행 조건을 직관적인 방법으로 기술할 수 있어야 한다.
;; 3. 추후 제약 조건의 추가 적용을 위해 고차 함수로 구성되어야 한다.

;; contract에 대한 구조 만들기
;; 오직 양수만 받아서 2를 곱한 값을 리턴하는 함수에 대한 제약 조건
(contract doubler
          [x]
          (:require
           (pos? x))
          (:ensure
           (= (* 2 x) %)))

;; 최상위 레벨 매크로 contract
(declare collect-bodies)

(defmacro contract [name & forms]
  (list* `fn name (collect-bodies forms)))

;; contract가 리턴할 함수의 형태
;; (fn doubler
;;   ([f x]
;;    {:post [(= (* 2 x) %)]
;;     :pre [(pos? x)]}
;;    (f x)))

;; 여러개의 인자를 받을수 있는 함수 정의 구문을 허용 해야 한다.
;; 함수마다 하나 이상의 제약 조건들을 심벌 벡터로 구분하여 입력 받을수 있도록 해야 한다.
;; 우선은 collect-bodies를 구현
;; 주된 작업은 세 개 단위로 분할되어 있는 contract 몸체의 리스트를 구성 하는 것이다.
;; 분할된 각 요소들은 인자 목록과 contract의 require, ensures를 표현 한다.

(declare build-contract)

(defn collect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))

;; contract의 보조 함수 build-contract
(defn build-contract [c]
  (let [args (first c)]
    (list
     (into '[f] args)
     (apply merge
            (for [con (rest c)]
              (cond (= (first con) 'require)
                    (assoc {} :pre (vec (rest con)))
                    (= (first con) 'ensure)
                    (assoc {} :post (vec (rest con)))
                    :else (throw (Exception.
                                  (str "Unknown tag "
                                       (first con)))))))
     (list* 'f args))))

;; contract 함수와 제약 조건 함수 구성
(def doubler-contract ;; contract 정의
  (contract doubler
            [x]
            (require
             (pos? x))
            (ensure
             (= (* 2 x) %))))

;; 올바른 사용 테스트
(def times2 (partial doubler-contract #(* 2 %)))
(times2 9)

;; 잘못된 사용 테스트 - 정의된 contract를 만족사지 못한다.
;; Assert failed: (= (* 2 x) %)
(def times3 (partial doubler-contract #(* 3 %)))
(times3 9)

;; 여러개의 인자를 받는 함수를 위한 contract
(def doubler-contract2
  (contract doubler
            [x]
            (require
             (pos? x))
            (ensure
             (= (* 2 x) %))
            [x y]
            (require
             (pos? x)
             (pos? y))
            (ensure
             (= (* 2 (+ x y)) %))))

((partial doubler-contract2 #(* 2 (+ %1 %2))) 2 3)
((partial doubler-contract2 #(+ %1 %1 %2 %2)) 2 3)
((partial doubler-contract2 #(* 3 (+ %1 %2))) 2 3)

;; 매크로 사용은 가능한 최소한으로 한다.
