(ns lazy)

;; 클로저는 부분적으로 게으른 언어이다.

;; 아래 코드는 즉시 평가 된다.
(- 13 (+ 2 2))

(defn and-chain [x y z]
  (and x y z (do (println "Made it!") :all-truthy)))

(and-chain () 42 true)
(and-chain true false true)

;; 지연 시퀀스 이해
(defn rec-step [[x & xs]]
  (if x
    [x (rec-step xs)]
    []))

(rec-step [1 2 3 4])

;; 큰 수를 넣을 경우에는 스텍 오버플로우 오류 발생
(rec-step (range 200000))

;; next 대신 rest 사용?
;; next는 정의시 한번 더 실행됨
;; rest는 그렇지 않음
;; 그래서 실제 실행될때 next는 한번 덜 실행 됨
;; 지금은 버전에서는(1.11.4) 점 갯수 출력이 차이 없는 것 같음
(def very-lazy (-> (iterate #(do (print \.)
                                 (inc %)) 1)
                   rest
                   rest
                   rest))

(def less-lazy (-> (iterate #(do (print \.)
                                 (inc %)) 1)
                   next
                   next
                   next))

(println (first very-lazy))
(println (first less-lazy))

;; 지연 시퀀스와 rest 사용하기
(defn lz-rec-step [s]
  (lazy-seq
   (if (seq s)
     [(first s) (lz-rec-step (rest s))]
     [])))

(lz-rec-step [1 2 3 4])

(class (lz-rec-step [1 2 3 4]))

(dorun (lz-rec-step (range 200000)))

;; 청크 시퀀스 생성 지원이 제외된 range
;; 현재 버전에서는 헤드를 무시 하지 않는다.
(defn simple-range [i limit]
  (lazy-seq
   (when (< i limit)
     (println "REALIZED")
     (cons i (simple-range (inc i) limit)))))

;; 실체화 되는 횟수를 확인하는 코드
(def range-ten (simple-range 0 9))

;; 처음 실행되므로 1번 실체화가 된다.
;; REALIZED
;; 0
(first range-ten)

;; 위에서 한번 실체화 됐으므로 8번 실체화를 거쳐 마지막 값을 리턴 한다.
;; REALIZED
;; REALIZED
;; REALIZED
;; REALIZED
;; REALIZED
;; REALIZED
;; REALIZED
;; REALIZED
;; 8
(last range-ten)

;; last 호출로 전체 실체화가 되었으므로 실체화 되지 않고 값을 리턴 한다.
;; 1
(second range-ten)

;; 무한 시퀀스 활용
;; 선언적 솔루션을 만드는데 도움이 된다.
(defn triangle [n]
  (/ (* n (+ n 1)) 2))

(triangle 10)

(map triangle (range 1 11))

(comment
;; 선언적 솔루션 작성을 위한 시퀀스 활용
  (def tri-nums (map triangle (iterate inc 1)))

;; 첫 10개의 삼각수 계산
  (take 10 tri-nums)

;; 첫 10개 짝수에 대한 삼각수 계산
  (take 10 (filter even? tri-nums))

;; 가우스가 계산 했던 숫자
  (nth tri-nums 99)

;; 연산 조합
  (double (reduce + (take 1000 (map / tri-nums))))

;; 10000보다 큰 첫 숫자 2개
  (take 2 (drop-while #(< % 10000) tri-nums)))

;; if-let, when-let
(if :truthy-thing
  (let [res :truthy-thing] (println res)))

(if-let [res :truthy-thing] (println res))

;; delay와 force 매크로
;; force는 delay를 사용하기 전까지 평가를 미룰 수 있다.
;; 즉, 필요 할때까지 평가를 마루고 필요한 상황시에 delay로 평가 한다.
(defn defer-expensive [cheap expensive]
  (if-let [good-enough (force cheap)]
    good-enough
    (force expensive)))

(defer-expensive (delay :cheap)
                 (delay (do (Thread/sleep 5000) :expensive)))

(defer-expensive (delay false)
                 (delay (do (Thread/sleep 5000) :expensive)))

;; 지연시퀀스 버전 삼각수
;; 헤드 중심(head-strict)의 지연 구조
;; lazy-seq가 head, tail 모두를 지연 하는 것과는 차이가 있다.
(defn inf-triangles [n]
  {:head (triangle n)
   :tail (delay (inf-triangles (inc n)))})

(defn head [l] (:head l))
(defn tail [l] (force (:tail l)))

(def tri-nums (inf-triangles 1))

(head tri-nums)
(head (tail tri-nums))
(head (tail (tail tri-nums)))

;; 좀 더 복잡한 함수를 기반으로 하여 사용 해보기
(defn taker [n l]
  (loop [t n src l ret []]
    (if (zero? t)
      ret
      (recur (dec t) (tail src) (conj ret (head src))))))

(defn nthr [l n]
  (if (zero? n)
    (head l)
    (recur (tail l) (dec n))))

(taker 10 tri-nums)
(nthr tri-nums 99)





