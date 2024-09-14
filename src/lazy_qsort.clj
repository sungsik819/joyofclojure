(ns lazy-qsort)

;; n을 입력 받아 0부터 n까지의 숫자를 랜덤으로 나열한 시퀀스(seq)를 생성 한다.
(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

(rand-ints 10)

(defn sort-parts [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn qsort [xs]
  (sort-parts (list xs)))

(qsort (rand-ints 3))

;; 정렬된 10개를 가져오는데 어떻게 이렇게 빨리 결과가 나올까?
(take 10 (qsort (rand-ints 10000)))

;; 코드 연습
(let [[part & parts] '((1) 2 (4 3))]
  (println part))

(list* (filter #(< % 2) '(1 4 3)) 2 (remove #(< % 2) '(1 4 3)) '())