(ns persistent)

;; 영속성을 확인하기 위한 자바 배열 사용
(def ds (into-array [:willie :barnabas :adam]))
(seq ds)

;; 자바 배열이기 때문에 실제 값 교체가 가능 하다
(aset ds 1 :quentin)
(seq ds)

;; 클로저 컬렉션을 사용하면 다르게 동작 한다.
(def ds [:willie :barnabas :adam])

(def ds1 (replace {:barnabas :quentin} ds))

;; ds의 값은 원래 값으로 유지된다.
ds

;; ds1은 원본 백터의 수정된 버전이다.
ds1