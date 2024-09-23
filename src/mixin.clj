(ns mixin)

;; 클로저 스타일 믹스인
(use 'clojure.string)

(defprotocol StringOps
  (rev [s])
  (upp [s]))

(extend-type String
  StringOps
  (rev [s] (clojure.string/reverse s)))

(rev "Works")

(extend-type String
  StringOps
  (upp [s] (clojure.string/upper-case s)))

(upp "Works")

;; 오류 발생
;; 한번에 모든 함수가 확장 되어야 한다.
;; 프로토콜 확장은 함수 단위가 아니라 프로토콜 전체에서 이루어 진다.
(rev "Works?")

;; 클로저에서의 믹스인
;; 프로토콜 함수 구현을 포함하고 있는 맵을 생성하여 참조하는 방식
(def rev-mixin {:rev clojure.string/reverse})
;; (def upp-mixin {:upp (fn [this] (.toUpperCase this))})
(def upp-mixin {:upp clojure.string/upper-case})
(def fully-mixin (merge upp-mixin rev-mixin))
(extend String StringOps fully-mixin)

(-> "Works" upp rev)