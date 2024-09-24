(ns protocol
  (:require [record :refer [xconj xseq sample-tree]])
  (:import [record TreeNode]))

;; 프로토콜
(defprotocol FIXO
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))

;; 타입 확장
(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (xconj node value)))

(xseq (fixo-push sample-tree 5/2))

;; FIXO를 클로저 백터로 확장
;; 아래 클래스를 상속받는 모든 클래스에 적용 된다.
(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value]
    (conj vector value)))

(fixo-push [2 3 4 5 6] 5/2)

;; 클로저의 다형성은 클래스가 아닌 프로토콜 함수들 안에서 형성된다.

;; nil도 확장이 가능하다
;; 프로토콜 메서드가 첫번째 인자에 디스패치 되기 때문에 오류가 발생
;; No implementation of method: 
;; :fixo-push of protocol: #'protocol/FIXO found for class: nil 
(reduce fixo-push nil [3 5 2 4 6 0])

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))

(xseq (reduce fixo-push nil [3 5 2 4 6 0]))

;; extend는 항상 프로토콜과 연관되어 있다.

;; TreeNode와 백터에 대한 완전한 FIXO 구현
(extend-type TreeNode
  FIXO
  (fixo-push [node value] ;; xconj에 위임
    (xconj node value))
  (fixo-peek [node] ;; 왼쪽 노드를 반복하여 가장 작은 것은 찾는다.
    (if (:l node)
      (recur (:l node))
      (:val node)))
  (fixo-pop [node] ;; 삭제된 항목 완쪽으로 새 경로 구성
    (if (:l node)
      (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
      (:r node))))

(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value]
    (conj vector value))
  (fixo-peek [vector]
    (peek vector))
  (fixo-pop [vector]
    (pop vector)))

;; 메서드 구현 공유
;; 클로저는 구현을 상속하는 것을 권장하지 않는다.
;; 유사한 객체들이 같은 프로토콜 메서드를 구현 할 때 어떻게 하면 코드의 반복을 피할 수 있을까?

;; 프로토콜의 메서드들을 정규화된 함수로 작성
;; 이 방법은 함수가 프로토콜의 메서드로 정의 될 수 있을 때만 가능 하다.
(defn fixo-into [c1 c2]
  (reduce fixo-push c1 c2))

(xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))

(seq (fixo-into [5] [2 4 6 7]))

;; 맵을 사용하여 확장
;; mixin에서 사용한 방법
(def tree-node-fixo
  {:fixo-push (fn [node value]
                (xconj node value))
   :fixo-peek (fn [node]
                (if (:l node)
                  (recur (:l node))
                  (:val node)))
   :fixo-pop (fn [node]
               (if (:l node)
                 (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
                 (:r node)))})

(extend TreeNode FIXO tree-node-fixo)

(xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))

;; reify 매크로
;; 클래스를 생성해주는 팩토리 역할을 한다.
(defn fixed-fixo
  ([limit] (fixed-fixo limit []))
  ([limit vector]
   (reify FIXO
     (fixo-push [this value]
       (if (< (count vector) limit)
         (fixed-fixo limit (conj vector value))
         this))
     (fixo-peek [_]
       (peek vector))
     (fixo-pop [_]
       (pop vector)))))

(def stack-5 (fixed-fixo 5))

(-> stack-5
    (.fixo-push 1)
    (.fixo-push 2)
    (.fixo-push 3)
    (.fixo-push 4)
    (.fixo-push 5)
    (.fixo-push 6)
    (.fixo-peek))

;; 네임스페이스 메서드
;; 자바나 C++에서 클래스의 모든 메서드들은 동일한 네임스페이스를 공유한다.
;; 클로저는 메서드들이 항상 프로토콜과 같은 네임스페이스를 사용한다.
;; 그래서 동일한 메서드라도 extend, extend-typ 등을 통해 서로 다른 프로토콜로 확장이 가능하다
;; 메서드들이 프로토콜과 네임스페이스를 공유하기 때문에 두개의 다른 프로토콜이
;; 같은 네임스페이스 상에 있다면 동일한 메서드 명을 사용할 수 없다.
;; 한 프로토콜을 다른 네임스페이스로 이동시키거나 좀 더 세부적인 메서드 명을 사용하여 문제를 해결 한다.

;; defrecord에서 메서드 구현하기
;; extend로 구현한 것 보다 빠르게 동작 한다.
;; (:val t) 대신 val로 가능
;; 자바 인터페이스로 정의된 부분을 확장 할 수 있다. 
;; java.lang.Object를 확장 할 수 있다.
(defrecord TreeNode2 [val l r]
  FIXO
  (fixo-push [t v]
    (if (< v val)
      (TreeNode2. val (fixo-push l v) r)
      (TreeNode2. val l (fixo-push r v))))
  (fixo-peek [t]
    (if l
      (fixo-peek l)
      val))
  (fixo-pop [t]
    (if l
      (TreeNode2. val (fixo-pop l) r)
      r)))

(def sample-tree2 (reduce fixo-push (TreeNode2. 3 nil nil) [5 2 4 6]))
(xseq sample-tree2)