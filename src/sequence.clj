(ns sequence)

;; 컬렉션 - 복합 데이터 타입 예) [1 2], {:a 1}, #{1 2}, 리스트, 배열
;; 순차적 컬렉션 - 정렬된 일련의 값들 예) [1 2 3 4], (1 2 3 4)
;; 시퀀스(sequence) - 존재하거나 아직 존재하지 않을 수도 있는 순차적 컬렉션
;; 예) (map func collection)의 결과
;; 시퀀스(seq) - 컬렉션 탐색을 위한 간단한 API 예) first, rest, nil, ()
;; clojure.core/seq - 시퀀스(seq) API를 구현한 객체를 리턴하는 함수
;; 예) (seq []) => nil, (seq [1 2]) => (1 2)

;; 타입이 다르더라도 순차적 컬렉션이면 같다
(= [1 2 3] '(1 2 3)) ;; => true

;; 순서가 다르면 어떻게 될까?
(= [1 2 3] '(1 3 2)) ;; => false

;; 서로 다른 타입의 컬렉션이면 다르다
(= [1 2 3] #{1 2 3})

(class (hash-map :a 1))

(seq (hash-map :a 1))

(class (seq (hash-map :a 1)))

(seq (keys (hash-map :a 1)))

(class (keys (hash-map :a 1)))