(ns first-class-function)

;; 함수 구문 종류
;; 백터를 함수로 호출, 숫자들을 인자로 전달함
(map [:chthon :phthor :beowulf :grendel] #{0 3})

;; 일급 클래스 함수
;; 클로저는 소프트웨어 개발 문제에 대해 함수를 데이터에 적용한다는 관점으로 인식 한다.
;; 클로저의 함수도 데이터와 동등한 지위를 갖고 있으며, 함수도 일급 클래스 이다.

;; 일급 클래스란?
;; 필요에 의해 생성될 수 있다.
;; 데이터 구조 내에 저장될 수 있다.
;; 함수의 인자로 전달될 수 있다.
;; 함수의 결과 값으로 리턴 될 수 있다.

;; 필요에 의한 함수 생성 : 합성 사용하기 (comp)
(def fifth (comp first rest rest rest rest))
(fifth [1 2 3 4 5])

;; n번째를 인자로 받아 그 위치의 값을 찾는 함수
;; repeat으로 rest를 반복하여 n번째 값을 찾는다.
(defn fnth [n]
  (apply comp
         (cons first (take (dec n) (repeat rest)))))

((fnth 5) '[a b c d e])

;; comp를 사용하기 위해서는 각각의 함수를 합성 할 수 있도록 잘게 쪼개는 것이 중요하다.
(map (comp
      keyword
      #(.toLowerCase %)
      name)
     '(a B C))

;; 필요에 의한 함수 생성 : 부분 함수 사용하기 (partial)
;; 다른 함수의 일부로 적용하기 위한 함수가 필요할 경우

;; default로 5를 더하고 그 후에 원하는 값을 추가해서 계산 한다.
((partial + 5) 100 200)

(#(apply + 5 %&) 100 200)

;; complement로 불리언 값 변경하기
;; 참 또는 거짓 값을 받아서 반대 값을 리턴 한다.
;; 언제 사용 하는 것일까?
(let [truthiness (fn [v] v)]
  [((complement truthiness) true)
   ((complement truthiness) 42)
   ((complement truthiness) false)
   ((complement truthiness) nil)])

;; (comp not even?), #(not (even? %)) 과 같은 의미
((complement even?) 2)

;; defn 메타데이터의 형태
;; 모두 같은 의미 이다.
(comment
  (defn ^:private ^:dynamic sum [nums]
    (map + nums))

  (defn ^{:private true :dynamic true} sum [nums]
    (map + nums))

  (defn sum {:private true :dynamic true} [nums]
    (map + nums))

  (defn sum
    ([nums]
     (map + nums))
    {:private true :dynamic true}))

;; 함수를 데이터로 사용하기
;; 메타 데이터 사용
(defn join
  {:test (fn []
           (assert
            (= (join "," [1 2 3]) "1,3,3")))}
  [sep s]
  (apply str (interpose sep s)))

(use '[clojure.test :as t])
;; 위에서 생성한 join 함수에 :test가 정의되어 있고,
;; 그것이 run-tests 함수에 의해 실행 되었다.
(t/run-tests)