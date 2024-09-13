(ns map)

;; 해시맵
;; 키/값 쌍을 가지고 있다.
;; hash-map 함수로 생성 할 수 있다.
(hash-map :a 1 :b 2 :c 3 :d 4 :e 5)

;; 어떤 타입이라도 키로 사용 할 수 있고, 서로 다른 타입이어도 관계 없다.
(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(get m :a) (get m [1 2 3])]) ;; => [1 "4 5 6"]

;; 키를 함수로 사용이 가능 하다.
(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(m :a) (m [1 2 3])])

;; seq를 사용시 맵 엔트리의 시퀀스가 리턴 된다.
(seq {:a 1 :b 2}) ;; => ([:a 1] [:b 2])

;; 백터로 키/값 쌍으로 구성 되면 해시맵으로도 생성이 가능 하다.
(into {} [[:a 1] [:b 2]])

;; 다른 종류의 키/값을 키로 가지는 백터의 경우도 가능한가? => 가능하다
(into {} [[:a 1] [1 :b] [[1 2 3] "4 5 6"]])

;; 벡터가 아닌 쌍이라도 가능하다.
;; 내부는 무조건 벡터여야 가능 하다
(into {} (map vec '[(:a 1) (:b 2)]))

;; 내부의 구조가 벡터가 아니면 오류가 발생 한다.
(into {} '[(:a 1) (:b 2)])

;; apply를 사용하면 그룹으로 묶이기 않아도 생성이 가능 하다.
(apply hash-map [:a 1 :b 2])

;; zipmap을 사용하여 생성
;; sorted-map으로 구현되도록 내부구조가 바뀌었나?
;; 다르게 생성되지 않네
(zipmap [:a :b] [1 2])

;; sorted-map
(sorted-map :thx 1138 :r2d 2)
(sorted-map "bac" 2 "abc" 9)

;; key간에 비교가 어려운 경우 sorted-map-by를 사용 한다.
;; 아래는 key의(bac, abc) 두번째 값을 비교 한다.
(sorted-map-by #(compare (subs %1 1) (subs %2 1)) "bac" 2 "abc" 9)

;; 타입이 다른 키를 지원하지 않는다.
(sorted-map :a 1 "b" 2)

;; subseq, rsubseq
;; 정렬된 맵, set에서 사용 가능
(subseq (sorted-map :a 1 :b 2 :c 3) > :a)
(rsubseq (sorted-set 1 2 3 4 5 6 7) > 1 < 4)

;; 숫자 키를 다루기
;; 정렬된 맵에서는 같은 객체로 처리,
;; 일반 맵에서는 다른 객체로 처리
(assoc {1 :int} 1.0 :float) ;; => {1 :int, 1.0 :float}
(assoc (sorted-map 1 :int) 1.0 :float) ;; => {1 :float}

;; 해시맵은 입력 순서가 유지 되지 않는다.
(seq (hash-map :a 1 :b 2 :c 3))

;; 배열맵으로 입력 순서 유지하기가 가능하다.
(seq (array-map :a 1 :b 2 :c 3))




