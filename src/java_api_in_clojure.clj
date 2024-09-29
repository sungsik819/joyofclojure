(ns java-api-in-clojure)

;; java.util.List 인터페이스
;; 클로저의 순차적 컬렉션은 java.util.List 인터페이스의 불변적 부분에 대응
;; java.util.Collection, java.lang.Iterable 인터페이스를 확장 한다.
;; java.util.List와 시퀀스(seq)간의 대응
(.get '[a b c] 1)

(.get (repeat :a) 138)

(.containsAll '[a b c] '[b c])

;; 시퀀스는 가변적이지 않음
(.add '[a b c] 'd)

;; 시퀀스는 기존의 자바 컬렉션에서 제공하는 가변적 API들은 제공하지 않는다.
(java.util.Collection/sort [3 4 2 1])

;; java.lang.Comparable 인터페이스
;; Comparator 인터페이스의 친척
;; Comparator는 두개의 다른 객체를 비교
;; Comparable은 자기 자신을 다른 객체와 비교

(.compareTo [:a] [:a]) ;; => 0

(.compareTo [:a :b] [:a]) ;; => 1

(.compareTo [:a :b] [:a :b :c]) ;; => -1

(sort [[:a :b :c] [:a] [:a :b]])

;; 클로저의 백터는 .compareTo를 제공하는 
;; java.lang.Comparable 인터페이스를 구현 하는
;; 유일한 컬렉션 타입이다.
(.compareTo [1 2 3] '(1 2 3))

;; java.util.RandomAccess 인터페이스
;; 상수 시간 안의 요소에 접근 할 수 있는 데이터 타입
;; 반복자가 아닌 .get 메서드에 의해 수행 된다.
;; 백터만 보장 된다.
(.get `[a b c] 2)

;; 이것도 되는건 아닌가?
(.get '("a" "b" "c") 1)

;; java.util.Collection 인터페이스
;; 자바의 코어 컬렉션 API와 같이 동작 할 수 있게 된다.
;; 자바 컬렉션 API 사용을 위해 클로저 시퀀스를 가변적 시퀀스 구성을 위한 모델로
;; 사용할 때 이 방식을 활용 한다.
;; 이미 core 라이브러리에 shuffle이 있긴 하다
(defn shuffle [coll]
  (seq (doto (java.util.ArrayList. coll)
         java.util.Collections/shuffle)))

(shuffle (range 10))

;; java.util.map 인터페이스
;; 클로저 맵도 자바의 맵과 유서성을 갖는다.
;; 변경을 일으키지 않는 상황에서 사용 할 수 있다.
(java.util.Collections/unmodifiableMap
 (doto (java.util.HashMap.) (.put :a 1)))

(into {} (doto (java.util.HashMap.) (.put :a 1)))

;; 두 가지 경우 모두 맵의 맵-엔트리 클래스를 수정하려 하면 예외가 발생 한다.

;; java.util.Set 인터페이스
(def x (java.awt.Point. 0 0))
(def y (java.awt.Point. 0 42))
(def points #{x y})

;; 가변성을 건드리면 클로저 set의 값이 수정 되어 깨진다.
(.setLocation y 0 0)

;; 클로저 셋의 값도 수정 되었다.
points