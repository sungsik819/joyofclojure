(ns transient)

;; 트랜션트
;; 단기성 가비지 

(reduce merge [{1 3} {1 2} {3 4} {3 5}])

;; 가변적 컬렉션과 비교한 트랜션트의 효율성
(defn zencat1 [x y]
  (loop [src y ret x]
    (if (seq src)
      (recur (next src) (conj ret (first src)))
      ret)))

(zencat1 [1 2 3] [4 5 6])

;; 빠르네..
;; "Elapsed time: 37.2595 msecs"
(time (dotimes [_ 100000] (zencat1 [1 2 3] [4 5 6])))

;; 트랜션트 사용
(defn zencat2 [x y]
  (loop [src y ret (transient x)] ;; 트랜션트 생성
    (if src
      (recur (next src) (conj! ret (first src))) ;; 트랜션트 conj! 사용
      (persistent! ret)))) ;; 트랜션트 리턴

(time (dotimes [_ 100000] (zencat2 [1 2 3] [4 5 6])))

;; 측정의 다른 방법
;; 크기가 큰 벡터 간 연결
(def bv (vec (range 1e6)))

(first (time (zencat1 bv bv)))

(first (time (zencat2 bv bv)))

;; 트랜션트 사용에도 비용이 있으므로 유의 해야 한다.
