(ns lexical-closure)

;; 렉시컬 클로저(closure)
;; 생성된 위치의 컨텍스트에서 로컬에 접근 할 수 있는 함수
(def times-two
  (let [x 2]
    (fn [y] (* y x))))

(times-two 5)

;; 가변적인 것
;; .andAndGet 함수는 입력 받은 값을 저장하고 그 값을 리턴 한다. 
;; 아래 함수는 누적되는 함수로, 순수한 함수가 아니다.
(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))

(add-and-get 2)
(add-and-get 2)
(add-and-get 7)

;; 클로저(closure)를 리턴하는 함수
(defn times-n [n]
  (let [x n]
    (fn [y] (* y x))))

(def times-four (times-n 4))
(times-four 10)

;; 인자 감싸기
;; 위 times-n을 let을 없애면 아래와 같다.
;; (defn times-n [n]
;;   (fn [y] (* y n)))

(defn divisible [denom]
  (fn [num]
    (zero? (rem num denom))))

;; 바로 호출
((divisible 3) 6)

((divisible 3) 7)

;; 클로저(closure)를 함수로 전달 하기
(filter even? (range 10))

(filter (divisible 4) (range 10))

;; 재 사용성을 고려 하여 위와 같이 할지 밑에 처럼 할지 고려 하는 것이 필요함
(defn filter-divisible [denom s]
  (filter (fn [num] (zero? (rem num denom))) s))

(filter-divisible 4 (range 10))

;; 익명 함수를 아래처럼 간단하게 사용 가능 하다.
(defn filter-divisible2 [denom s]
  (filter #(zero? (rem % denom)) s))

(filter-divisible2 5 (range 20))

;; 클로저(closure) 컨텍스트 공유
(def bearings [{:x 0 :y 1}
               {:x 1 :y 0}
               {:x 0 :y -1}
               {:x -1 :y 0}])

(defn forward [x y bearing-num]
  [(+ x (:x (bearings bearing-num)))
   (+ y (:y (bearings bearing-num)))])

(forward 5 5 0)

(forward 5 5 1)

(forward 5 5 2)

(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(:coords (bot 5 5 0))
(:bearing (bot 5 5 0))
(:coords ((:forward (bot 5 5 0))))


;; bot과 구조는 같지만 반대로 움직이는 함수
(defn mirror-bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (mirror-bot (- x (:x (bearings bearing-num)))
                               (- y (:y (bearings bearing-num)))
                               bearing-num))
   :turn-right (fn [] (mirror-bot x y (mod (- 1 bearing-num) 4)))
   :turn-left (fn [] (mirror-bot x y (mod (+ 1 bearing-num) 4)))})





