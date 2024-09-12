(ns vector)

;; 만들기
;; 다른 종류의 컬렉션에서 백터로
(vec (range 10))

;; 이미 생성된 백터에 다른 값 넣기
(let [my-vector [:a :b :c]]
  (into my-vector (range 10)))

;; 기본 타입 지정
;; 타입이 다른 데이터인 경우 강제로 변환 된다.
(into (vector-of :int) [Math/PI 2 1.3])

(into (vector-of :char) [100 101 102])

;; int의 값 범위를 넘을 경우 예외 발생
(int (vector-of :int [1 2 389048203948230957329048032948230]))

;; 값을 상수 시간안에 접근
(def a-to-j (vec (map char (range 65 75))))

a-to-j

;; 모두 동일하게 \E 리턴
(nth a-to-j 4)
(get a-to-j 4)
(a-to-j 4)

;; 벡터가 nil인 경우 - nth : nil 리턴, get : nil 리턴, 함수로서의 벡터 : 예외 발생
;; 인덱스 범위 밖인 경우 - nth : 예외 발생 또는 not found 리턴, get : nil 리턴 , 함수로서의 벡터 : 예외 발생
;; "not found" 인자 지원 여부
;; nth : 지원 (nth [] 9 :whoops)
;; get : 지원 (get [] 9 :whoops)
;; 함수로서의 벡터 : 미지원

;; 벡터는 색인되어 있어서 좌,우 어떤 방향으로든 효율적으로 동작
(seq a-to-j)
(rseq a-to-j)

;; 값 변경
;; 구조 공유 하여 값 변경 한다.
(assoc a-to-j 4 "no longer E")

;; 내부적으로 assoc 사용
(replace {2 :a 4 :b} [1 2 3 2 3 4])

;; 백터와 맵의 내부구조에서 사용
(def matrix [[1 2 3]
             [4 5 6]
             [7 8 9]])

(get-in matrix [1 2])
(assoc-in matrix [1 2] 'x)

;; 내부에 apply 함수로 동작 한다.
(update-in matrix [1 2] * 100)

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

(neighbors 3 [0 0])

(neighbors 3 [1 1])

(map #(get-in matrix %) (neighbors 3 [0 0]))

;; 스택으로 사용하기
;; 벡터를 스택으로써 사용하려면 아래의 함수를 사용하여 스택으로써의 용어를 맞춘다.
;; 항목을 추가할 때는 assoc 대신 conj
;; 값을 가져오려면 last 대신 peek
;; 꺼낼 때는 dissoc 대신 pop
(def my-stack [1 2 3])

(peek my-stack)

(pop my-stack)

(conj my-stack 4)

(+ (peek my-stack) (peek (pop my-stack)))

;; reverse와 벡터
;; 전통적인 리습 언어에서는 효율적이지만 clojure에서는 벡터를 사용한다.

;; reverse 사용시
(defn strict-map1 [f coll]
  (loop [coll coll
         acc nil]
    (if (empty? coll)
      (reverse acc)
      (recur (next coll)
             (cons (f (first coll)) acc)))))

(strict-map1 - (range 5))

;; 벡터 사용시
(defn strict-map2 [f coll]
  (loop [coll coll
         acc []]
    (if (empty? coll) acc
        (recur (next coll)
               (conj acc (f (first coll)))))))

(strict-map2 - (range 5))

;; 서브 벡터
(subvec a-to-j 3 6)

;; 맵 엔트리로서의 벡터
;; 맵의 속성 값은 벡터이므로 벡터로 제공되는 함수들을 사용 가능 하다
(first {:width 10 :height 20, :depth 15}) ;; => [:width 10]

(vector? (first {:width 10 :height 20, :depth 15})) ;; => true

;; 구조 분해도 가능 하다
(doseq [[dimension amount] {:width 10 :height 20 :depth 15}]
  (println (str (name dimension) ":") amount "inches"))

;; 벡터는 큐가 아니다.
;; 벡터는 셋이 아니다.


