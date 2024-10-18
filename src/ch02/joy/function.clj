(ns joy.function)

;; 함수
;; 익명 함수

;; 정의
(fn [x y]
  (println "Making a set")
  #{x y})

;; 호출
((fn [x y]
   (println "Making a set")
   #{x y}) 1 2)

;; 함수 생성
(def make-set
  (fn [x y]
    (println "Making a set")
    #{x y}))

(make-set 1 2)

;; 함수 생성의 더 편한 방법
(defn make-set2
  "Takes two values and makes a set from them."
  [x y]
  (println "Making a set")
  #{x y})

;; 인수를 여러개 가지는 함수
;; 애리티 오버로딩
(defn make-set3
  ([x] #{x})
  ([x y] #{x y}))

(make-set3 42)

;; (make-set3 1 2 3)

;; 가변 인자
(defn arity2+ [first second & more]
  (vector first second more))

(arity2+ 1 2)
(arity2+ 1 2 3 4)

;; 너무 적은 인자로 호출하면 오류 발생
;; (arity2+ 1)

;; #()로 익명 함수 만들기

;; 인자 없음
(def make-list0 #(list))
(make-list0)

;; 2개의 인자만 받음
(def make-list2 #(list %1 %2))
(make-list2 1 2)

;; 2개 이상의 인자를 받음
(def make-list2+ #(list %1 %2 %&))
(make-list2+ 1 2 3 4 5)