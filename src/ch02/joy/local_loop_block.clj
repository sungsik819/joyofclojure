(ns joy.local-loop-block)

;; 블록
;; do 구문은 모든 표현식을 평가하고 마지막 표현식의 결과만 리턴
;; 부수 효과가 발생할 여지가 있다.
(do
  (def x 5)
  (def y 4)
  (+ x y)
  [x y])

;; 로컬
;; do 구문처럼 마지막의 결과만 리턴 된다.
(let [r 5
      pi 3.1415
      r-squard (* r r)]
  (println "radius is" r)
  (* pi r-squard))

;; 반복

;; 재귀
(defn print-down-from [x]
  (when (pos? x)
    (println x)
    (recur (dec x))))

(defn sum-down-from [sum x]
  (if (pos? x)
    (recur (+ sum x) (dec x))
    sum))

(sum-down-from 0 10)

;; 루프
(defn sum-down-from2 [init-x]
  (loop [sum 0 x init-x]
    (if (pos? x)
      (recur (+ sum x) (dec x))
      sum)))

;; 꼬리 재귀
;; recur 구문은 함수나 loop의 꼬리 위치에만 올 수 있다.
;; 꼬리 위치란 전체 표현식의 리턴 값이 될 때이다.