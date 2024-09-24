(ns type
  (:require [protocol :refer [FIXO]]))

;; deftype으로 밑바닥부터 구현하기
;; 기존에 동작하던 것들이 동작하지 않을 수 있으므로 재구현 해야 할 수도 있다.
;; xseq를 사용했었는데 seq로도 가능 하다
;; 하지만 defrecord로 정의하면 오류가 발생 한다.
(comment
  (defrecord InfiniteConstant [i]
    clojure.lang.ISeq
    (seq [this]
      (lazy-seq (cons i (seq this))))))

;; 타입으로 구현하면 오류가 발생하지 않는다.
(deftype InfiniteConstant [i]
  clojure.lang.ISeq
  (seq [this]
    (lazy-seq (cons i (seq this)))))

(take 3 (InfiniteConstant. 5))

;; 그러나 키워드 조회나 assoc, dissoc 등은 구현되어 있지 않게 된다.
(:i (InfiniteConstant. 5))

;; 선언된 필드는 공개적으로 접근이 가능 하다.
(.i (InfiniteConstant. 5))

(deftype TreeNode [val l r]
  FIXO
  (fixo-push [_ v]
    (if (< v val)
      (TreeNode. val (.fixo-push l v) r)
      (TreeNode. val l (.fixo-push r v))))
  (fixo-peek [_]
    (if l
      (.fixo-peek l)
      val))
  (fixo-pop [_]
    (if l
      (TreeNode. val (.fixo-pop l) r)
      r))

  clojure.lang.IPersistentStack
  (cons [this v] (.fixo-push this v))
  (peek [this] (.fixo-peek this))
  (pop [this] (.fixo-pop this))

  clojure.lang.Seqable
  (seq [t]
    (concat (seq l) [val] (seq r))))

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))

(def rootnode (TreeNode. 3 nil nil))
(seq rootnode)

;; NullPointerException 발생으로 테스트 되지 않음 
(def sample-tree2 (into rootnode [5 2 4 6]))

;; deftype의 사용은 좀 더 잘 알게된 후에 사용하는게 좋을 듯 함