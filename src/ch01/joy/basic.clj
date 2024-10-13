(ns joy.basic)

;; 우좌 방향으로 계산식을 평가하는 함수
(defn r->lfix
  ([a op b] (op a b))
  ([a op1 b op2 c] (op1 a (op2 b c)))
  ([a op1 b op2 c op3 d] (op1 a (op2 b (op3 c d)))))

(comment
  (r->lfix 1 + 2)
  (r->lfix 1 + 2 + 3)
  (r->lfix 1 + 2 * 3)
  (r->lfix 10 * 2 + 3))

;; 좌우 방향으로 계산식을 평가하는 함수
(defn l->rfix
  ([a op b] (op a b))
  ([a op1 b op2 c] (op2 c (op1 a b)))
  ([a op1 b op2 c op3 d] (op3 d (op2 c (op1 a b)))))

(comment
  (l->rfix 10 * 2 + 3)
  (l->rfix 1 + 2 + 3)
  (l->rfix 1 + 2 * 3))

;; 연산자 우선순위 표현
(def order {+ 0 - 0
            * 1 / 1})

;; 연산자 우선순위에 따라 평가되도록 변경되는 함수
(defn infix3 [a op1 b op2 c]
  (if (< (get order op1) (get order op2))
    (r->lfix a op1 b op2 c)
    (l->rfix a op1 b op2 c)))

(comment
  (infix3 1 + 2 * 3)
  (infix3 10 * 2 + 3)
  (< 0 1 3 9 36 42 108)
  (< 0 1 2 3 4 1 2 1))

;; 객체 지향으로 할 수 있는 것은 클로저도 대부분 제공 한다.
;; 다형적인 Concatenatable 프로토콜
(defprotocol Concatenatable
  (cat [this other]))

;; 다형성과 표현상의 문제
;; String을 확장 가능하다
(extend-type String
  Concatenatable
  (cat [this other]
    (.concat this other)))

(cat "House" " of Leaves")

;; java.util.List 타입으로 확장도 가능하다.
(extend-type java.util.List
  Concatenatable
  (cat [this other]
    (concat this other)))

(cat [1 2 3] [4 5 6])

;; 많은 언어들이 위와 같이 확장이 어렵다.
;; 자바는 실제 구현을 포함한 클래스를 정의하기 전에 모든 메서드명과
;; 그 그룹(인터페이스)들을 먼저 정의해야 한다.
;; 이러한 한계는 표현 문제라 부른다.

;; 서브 타입과 인터페이스 지향 프로그래밍