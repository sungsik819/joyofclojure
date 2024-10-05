(ns ch05-pos)

;; pos 함수 구현
(defn pos [e coll]
  (let [cmp (if (map? coll)
              #(= (second %1) %2)
              #(= %1 %2))]
    (loop [s coll idx 0]
      (when (seq s)
        (if (cmp (first s) e)
          (if (map? coll)
            (first (first s))
            idx)
          (recur (next s) (inc idx)))))))

(pos 3 [:a 1 :b 2 :c 3 :d 4])
(pos :foo [:a 1 :b 2 :c 3 :d 4])
(pos 3 {:a 1 :b 2 :c 3 :d 4})

(pos \3 ":a 1 :b 2 :c 3 :d 4")

;; 위 pos 처럼 명시적으로 컬렉션을 지정하지 말고,
;; [[index value]...]처럼 쌍들의 시퀀스로 생각해 보자
(defn index [coll]
  (cond
    (map? coll) (seq coll)
    (set? coll) (map vector coll coll)
    :else (map vector (iterate inc 0) coll)))

(index [:a 1 :b 2 :c 3 :d 4])
(index {:a 1 :b 2 :c 3 :d 4})
(index #{:a 1 :b 2 :c 3 :d 4})

;; index 내부를 보면 map인 경우 seq를 사용하여 변환이 가능하다
(seq {:a 1 :b 2}) ;; => ([:a 1] [:b 2])

;; index를 사용하여 pos 구현
(defn pos2 [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(pos2 3 [:a 1 :b 2 :c 3 :d 4])
(pos2 3 {:a 1 :b 2 :c 3 :d 4})
(pos2 3 [:a 3 :b 3 :c 3 :d 4])
(pos2 3 {:a 3 :b 3 :c 3 :d 4})

;; pos가 서술식 함수를 받을 수 있도록 확장 한다.
(defn pos3 [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(pos3 #{3 4} {:a 1 :b 2 :c 3 :d 4})
(pos3 even? {:a 1 :b 2 :c 3 :d 4})
