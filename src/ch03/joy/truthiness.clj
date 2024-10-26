(ns joy.truthiness)

;; 참
;; nil과 false를 제외하면 모든 것은 참이다.
(if true :truthy :falsey) ;; => :truthy
(if [] :truthy :falsey) ;; => :truthy
(if nil :truthy :falsey) ;; => :falsey
(if false :truthy :falsey) ;; => :falsey

;; 불리언 객체 생성 하지 말것
(def evil-false (Boolean. "false")) ;; 절대 하지 말 것

evil-false ;; => false

(= false evil-false) ;; => true

;; 분기에서는 true 처리 된다.
(if evil-false :truthy :falsey) ;; :truthy

;; 문자열 파싱 하고 싶다면 아래를 사용 하자
(if (Boolean/valueOf "false") :truthy :falsey)