(ns udp
  (:refer-clojure :exclude [get])) ;; get을 구현할 것이기 때문에 clojure.core/get은 참조에서 제외

;; 보편적 디자인 패턴(UDP)의 클로저 멀티메서드 탐구
;; UDP 작동을 위해서는 beget, get, put, has?, forget 등과 같은 함수 필요

;; 맵을 받아서 다른 맵에 그 프로토타입의 참조를 연결한 새 맵을 리턴 한다.
(defn beget [this proto]
  (assoc this ::prototype proto))

(beget {:sub 0} {:super 1})

;; get 함수
;; 주어진 맵에 조회하는 값이 없으면 프로토타입 체인에서 찾는다.
(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(get (beget {:sub 0} {:super 1}) :super)

;; put 함수
;; 제공된 맵 수준에서만 동작 한다.
(def put assoc)

;; UDP의 기본적인 사용
(def cat {:likes-dogs true :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))

(get cat :likes-dogs)

(get morris :likes-dogs)

(get post-traumatic-morris :likes-dogs)

(get post-traumatic-morris :likes-9lives)