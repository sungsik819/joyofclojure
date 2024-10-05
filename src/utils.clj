(ns utils)

;; 5장에서 사용한 함수
;; 2차원 행렬의 특정 좌표에 대한 이웃을 찾는 함수
(defn neighbors
  ([size yx] (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
                        size
                        yx))
  ([deltas size yx]
   (filter (fn [new-yx]
             (every? #(< -1 % size) new-yx))
           (map #(vec (map + yx %))
                deltas))))

;; ch5의 pos 처럼 명시적으로 컬렉션을 지정하지 말고, 
;; [[index value]...]처럼 쌍들의 시퀀스로 생각해 보자
(defn index [coll]
  (cond
    (map? coll) (seq coll)
    (set? coll) (map vector coll coll)
    :else (map vector (iterate inc 0) coll)))

;; index를 사용하여 pos 구현
(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))