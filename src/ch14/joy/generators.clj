(ns joy.generators)

(def ascii (map char (range 65 (+ 65 26))))

;; 특정 갯수 갯수만큼의 알파벳을 임의 선택
(defn rand-str [sz alphabet]
  (apply str (repeatedly sz #(rand-nth alphabet))))

;; 심벌 생성
(def rand-sym #(symbol (rand-str %1 %2)))

;; 키워드 생성
(def rand-key #(keyword (rand-str %1 %2)))

;; 벡터와 같은 복합 구조 생성
(defn rand-vec [& generators]
  (into [] (map #(%) generators)))


;; 맵에도 적용
(defn rand-map [sz kgen vgen]
  (into {}
        (repeatedly sz #(rand-vec kgen vgen))))


(comment
  (rand-str 10 ascii)

  (rand-key 10 ascii)
  (rand-sym 10 ascii)

  (rand-vec #(rand-sym 5 ascii)
            #(rand-key 10 ascii)
            #(rand-int 1024))

  (rand-map 3 #(rand-key 5 ascii) #(rand-int 100)))

