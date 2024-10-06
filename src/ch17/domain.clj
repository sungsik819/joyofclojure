(ns domain
  (:require [clojure.set :as ra]
            [clojure.string :as str :only []]))

;; 도메인 내에서 생각하기

;; 아주 흔한 DSL
(def artists
  #{{:artist "Burial" :genre-id 1}
    {:artist "Magma" :genre-id 2}
    {:artist "Can" :genre-id 3}
    {:artist "Faust" :genre-id 3}
    {:artist "Iknoika" :genre-id 3}
    {:artist "Grouper"}})

(def genres
  #{{:genre-id 1 :genre-name "Dubstep"}
    {:genre-id 2 :genre-name "Zeuhl"}
    {:genre-id 3 :genre-name "Prog"}
    {:genre-id 4 :genre-name "Drone"}})

;; 클로저의 관계 대수 함수를 사용한 select * 예제
(def ALL identity)

(ra/select ALL genres)

;; id로 골라내기
(ra/select (fn [m] (#{1 3} (:genre-id m))) genres)

;; ids라는 커리 함수 만들기
(defn ids [& ids]
  (fn [m] ((set ids) (:genre-id m))))

(ra/select (ids 1 3) genres)

;; join
(take 2 (ra/select ALL (ra/join artists genres)))

;; 질의 작성을 위해 SQL과 유사한 DSL 구현 하기
;; 아래와 같이 SQL을 사용하고 싶다.
;; (defn fantasy-query [max]
;;   (SELECT [a b c]
;;     (FROM X
;;       (LEFT-JOIN Y :ON (= X.a Y.b)))
;;     (WHERE (< a 5) AND (< b max))))

;; 작성 할 DSL의 구성
;; 바인딩 추출하기
;; 전위 표기를 중위 표기로 변환 하기
;; 각각 더 복잡한 SQL 절 생성 하기

;; SQL과 비슷한 연산자들을 중위 연산자 위치로 재배치 하기
(defn shuffle-expr [expr]
  (if (coll? expr)
    (if (= (first expr) `unquote)
      "?"
      (let [[op & args] expr]
        (str "("
             (str/join (str " " op " ")
                       (map shuffle-expr args)) ")")))
    expr))

(shuffle-expr 42)

(shuffle-expr `(unquote max))

(read-string "~max") ;; ??

(shuffle-expr '(= X.a Y.b))


;; 재귀적 탐색이 적용 됨
(shuffle-expr '(AND (< a 5) (< b ~max)))

;; 임의의 깊이를 갖는 중첩된 표현식에 대해서도 동작 한다.
(shuffle-expr '(AND (< a 5) (OR (> b 0) (< b ~max))))

;; SQL과 비슷한 절 만들기
(defn process-where-clause [processor expr]
  (str " WHERE " (processor expr)))

(process-where-clause shuffle-expr '(AND (< a 5) (< b ~max)))

(defn process-left-join-clause [processor table _ expr]
  (str " LEFT JOIN " table
       " ON " (processor expr)))

(apply process-left-join-clause
       shuffle-expr
       '(Y :ON (= X.a Y.b)))

;; 별칭 사용
(let [LEFT-JOIN (partial process-left-join-clause shuffle-expr)]
  (LEFT-JOIN 'Y :ON '(= X.a Y.b)))

;; 규격을 괄호로 둘러싼다.
;; DSL의 기반을 코어 데이터 타입과 그 구문으로 시작하자
;; 데이터 처리기의 구성요소를 채워넣어 점진적으로 DSL을 구성하자

;; from 절 구현
(defn process-from-clause [processor table & joins]
  (apply str " FROM " table
         (map processor joins)))

(process-from-clause shuffle-expr 'X
                     (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))

;; select 절 구성
(defn process-select-clause [processor fields & clauses]
  (apply str "SELECT " (str/join ", " fields)
         (map processor clauses)))

;; 데이터 질의
(process-select-clause shuffle-expr
                       '[a b c]
                       (process-from-clause shuffle-expr 'X
                                            (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))
                       (process-where-clause shuffle-expr '(AND (< a 5) (< b ~max))))

(declare apply-syntax)

(def ^:dynamic *clause-map*
  {'SELECT (partial process-select-clause apply-syntax)
   'FROM (partial process-from-clause apply-syntax)
   'LEFT-JOIN (partial process-left-join-clause shuffle-expr)
   'WHERE (partial process-where-clause shuffle-expr)})

;; 처리기 목록에서 구문 처리기 조회하기
(defn apply-syntax [[op & args]]
  (apply (get *clause-map* op) args))

;; SQL과 유사한 SELECT 문 DSL 구성하기
(defmacro SELECT [& args]
  {:query (apply-syntax (cons 'SELECT args))
   :bindings (vec (for [n (tree-seq coll? seq args)
                        :when (and (coll? n)
                                   (= (first n) `unquote))]
                    (second n)))})

(defn example-query [max]
  (SELECT [a b c]
          (FROM X
                (LEFT-JOIN Y :ON (= X.a Y.b)))
          (WHERE (AND (< a 5) (< b ~max)))))

(example-query 9)

;; DSL에 대한 클로저의 접근 방식에 대해서
;; 클로저 매크로 작성자들은 괄호가 급증하게 될 것을 예상하고 가능하면 그 수를
;; 줄일 수 있도록 해야 한다.
;; 또 표현식들이 partition을 호출하는 것이나 마찬가지라면 명시적으로 묶지 않을 이유가 없다.
;; 클로저 경구 : 프로젝트가 복잡해지고 있다면 바닥부터 다시 시작하라.

;; 클로저에서는 하위 언어를 정의하여 구현하는 것부터 시작하여 상위 언어로 올라가는 방식이 일반적이다.