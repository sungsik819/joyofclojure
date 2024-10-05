(ns unification
  (:require [clojure.walk :as walk]))

;; 데이터 단일화 고려하기
;; 생각하는 데이터

;; 잠재적 동일성 또는 충족 가능성

;; 논리 변수를 식별하는 함수
(defn lvar?
  [x]
  (boolean
   (when (symbol? x)
     (re-matches #"^\?.*" (name x)))))

(lvar? '?x)
(lvar? 'a)
(lvar? 2)

;; 간단한 동일성 충족 함수
(defn satisfy1
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      :else nil)))

(satisfy1 '?something 2 {})
(satisfy1 2 '?something {})
(satisfy1 '?x '?y {})

(->> {}
     (satisfy1 '?x '?y)
     (satisfy1 '?x 1))

;; 시퀀스 동일성 충족 함수
(defn satisfy
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (not knowledge) nil
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      (every? seq? [L R])
      (satisfy (rest L)
               (rest R)
               (satisfy (first L)
                        (first R)
                        knowledge))
      :else nil)))

(satisfy '(1 2 3) '(1 ?something 3) {})

;; 중첨되어 있어도 가능
(satisfy '((((?something)))) '((((2)))) {})

;; 변수가 흩어져 있어도 가능 
(satisfy '(?x 2 3 (4 5 ?z))
         '(1 2 ?y (4 5 6))
         {})

(satisfy '?x '(?y) {})

;; 가능한 경우에만 동일성 충족
(satisfy '(?x 10000 3) '(1 2 ?y) {})

;; 치환
;; 데이터 구조를 살펴보면서 논리 변수들을 바인딩 값으로 치환 하기
(defn subst [term binds]
  (walk/prewalk
   (fn [expr]
     (if (lvar? expr)
       (or (binds expr) expr)
       expr))
   term))

(subst '(1 ?x 3) '{?x 2})

(subst '((((?x)))) '{?x 2})

(subst '[1 ?x 3] '{?x 2})

(subst '{:a ?x :b [1 ?x 3]} '{?x 2})

;; 바인딩이 제공 죄지 않은 경우
(subst '(1 ?x 3) '{})

(subst '(1 ?x 3) '{?x ?y})

;; 대상의 타입을 원래대로 보존 가능 해서
;; subst 기반으로 하면 웹 템플릿 시스템을 구성하는 것도 가능
(def page
  '[:html
    [:head [:title ?title]]
    [:body [:h1 ?title]]])

(subst page '{?title "Hi!"})

;; 단일화
;; 세가지 분리된 함수로 구성 된다.
;; 바인딩 추출 - satisfy의 기능
;; 치환 - subst의 기능
;; 두 구조의 혼합 - meld 기능

(defn meld [term1 term2]
  (->> {}
       (satisfy term1 term2)
       (subst term1)))

(meld '(1 ?x 3) '(1 2 ?y))

(meld '(1 ?x) '(?y (?y 2)))

;; 복잡한 상황에 대한 문제는 아직 있다.
