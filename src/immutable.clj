(ns immutable)

;; 영속적 구조
(def baselist (list :barnabas :adam))

(def lst1 (cons :willie baselist))
(def lst2 (cons :phoenix baselist))

lst1
lst2

;; 아래는 같은 것 뿐만 아니라 동일 하다
(= (next lst1) (next lst2))
(identical? (next lst1) (next lst2))

;; 트리 만들기
(defn xconj [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}))

(xconj nil 5)

;; 저장된 값보다 이전값이 들어오면 왼쪽에 넣는다.
(defn xconj [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}
    (< v (:val t)) {:val (:val t)
                    :L (xconj (:L t) v)
                    :R (:R t)}))

(def tree1 (xconj nil 5))
tree1

(def tree1 (xconj tree1 3))
tree1

(def tree1 (xconj tree1 2))
tree1

;; 트리를 조회하면서 시퀀스로 바꿔서 출력
(defn xseq [t]
  (when t
    (concat (xseq (:L t)) [(:val t)] (xseq (:R t)))))

(xseq tree1)

;; 노드의 값이 큰 값이 들어올 때는 오른쪽에 넣는다.
(defn xconj [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}
    (< v (:val t)) {:val (:val t)
                    :L (xconj (:L t) v)
                    :R (:R t)}
    :else {:val (:val t)
           :L (:L t)
           :R (xconj (:R t) v)}))

(def tree2 (xconj tree1 7))
(xseq tree2)
(identical? (:L tree1) (:L tree2))
tree2

;; 모든 변경은 새로운 루트 노드를 생성하고 새 값이 삽입될 위치의 트리 경로에 새 노드들을 추가한다.
;; 값과 변경되지 않는 가지가 복사되는 것이 아니라 기존의 노드에서 새 노드로 그 참조가 복사된다.
;; 스레드에 안전하다.

;; 위 구조로 데이터 구조의 공간을 절약 할 수 있지만 이것만으로는 부족하다
;; 메리 공간의 절약을 위한 지연 시퀀스 개념에 크게 의존 한다.
