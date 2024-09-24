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