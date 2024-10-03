(ns chunk-seq)

(def gimme #(do (print \.) %))

(take 1 (map gimme (range 32)))

(take 1 (map gimme (range 33)))

(take 1 (drop 32 (map gimme (range 64))))

(map #(do (print \.) %) [1 2 3])

;; one-at-a-time 지연의 회복
(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))

(take 1 (map gimme (seq1 (range 32))))

(take 1 (drop 32 (map gimme (seq1 (range 64)))))