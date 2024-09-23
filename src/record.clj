(ns record)

;; 레코드
;; 레코드는 빠르게 생성되고, 메모리를 적게 사용하며, 
;; 배열 맵이나 해시 맵보다 빠르게 키를 조회 할 수 있다.
;; 레코드 타입은 원시 타입을 저장 할 수 있다.
(comment
  (defrecord TreeNode [val l r])
  (TreeNode. 5 nil nil))

;; defrecord, deftype으로 선언된 것은 import로 불러야 한다.
;; defstruct는 요즘에는 거의 사용하지 않는다.

;; 맵과 레코드의 차이점
;; 레코드는 함수로 동작하지 않는다.
;; 레코드는 키/값의 맵핑이 맵과는 전혀 다르다.

;; 레코드는 다른 클로저 컬렉션들과 마찬가지로 with-meta, meta를 사용하는 메타데이터를 지원 한다.
;; 다른 필드가 필요한 경우에는 자바의 동적 컴파일 기능에 힘입어 레코드를 재정의할 수도 있다.

;; 레코드로 구성한 영속적 이진 트리
(defrecord TreeNode [val l r])

;; 트리에 추가
(defn xconj [t v]
  (cond
    (nil? t) (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t) (xconj (:l t) v) (:r t))
    :else (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

;; 트리를 시퀀스로 변환
(defn xseq [t]
  (when t
    (concat (xseq (:l t)) [(:val t)] (xseq (:r t)))))

;; 테스트
(def sample-tree (reduce xconj nil [3 5 2 4 6]))
(xseq sample-tree)

;; assoc, dissoc를 적용하면 일반 맵을 리턴 한다.
(dissoc (TreeNode. 5 nil nil) :l) ;; => {:val 5, :r nil}

;; 레코드는 프로토콜과 궁합이 잘 맞다.
