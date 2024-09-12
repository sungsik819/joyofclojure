(ns list)

;; lisp 의 car = clojure에서 first
;; lisp 의 cdr = clojure에서 next

(cons 1 '(2 3))

;; cons보다 더 큰 리스트를 얻을 수 있으므로 conj를 사용?
(conj '(2 3) 1)

;; 스택으로서의 리스트
;; 빈 리스트에서 pop을 실행하면 예외 발생
;; first, next는 빈 리스트에서도 사용 가능 하다.

(first '()) ;; => nil
(next '()) ;; => nil
(pop '()) ;; => Execution error (IllegalStateException) at list/eval10108 (REPL:17).

;; 요소가 한개인 리스트에서의 동작
(pop '(1)) ;; => ()
(rest '(1)) ;; => ()
(next '(1)) ;; => nil

;; 리스트 사용시 유의 사항
;; 리스트의 요소들을 인덱스로 조회하지 않는다.
;; nth는 사용 가능하지만 그 요소를 찾기위해 처음부터 조회 한다
;; 인덱스 조회를 사용하려면 벡터를 사용 한다.
;; 리스트는 큐도 아니다.