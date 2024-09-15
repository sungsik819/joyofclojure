(ns high-order-function)

;; 고차 함수 또는 고계 함수
;; 인자로 한개 이상의 함수를 취한다.
;; 함수를 결과로 리턴 한다.

;; 인자로서의 함수
(sort [1 5 7 0 -42 13])
(sort ["z" "x" "a" "aa"])
(sort [(java.util.Date.) (java.util.Date. 100)])

(sort [[1 2 3] [-1 0 1] [3 2 1]])

;; 다른 기준으로 정렬 할 경우
(sort > [7 1 4])

;; 서로 비교가 불가능한 경우에는 예외 발생 한다.
(sort ["z" "x" "a" "aa" 1 5 8])
;; => class java.lang.String cannot be cast to class java.lang.Number (java.lang.String and java.lang.Number are in module java.base of loader 'bootstrap')

(sort [{:age 99} {:age 13} {:age 7}])
;; => class clojure.lang.PersistentArrayMap cannot be cast to class java.lang.Comparable (clojure.lang.PersistentArrayMap is in unnamed module of loader 'app'; java.lang.Comparable is in module java.base of loader 'bootstrap')

;; 다른 기준일 경우 집합을 기준으로 졍렬하므로 원하는 결과가 나오지 않을 수 있다.
(sort [[:a 7] [:c 13] [:b 21]])

;; 두번째 요소를 기준으로 정렬 하려고 해도 동작하지 않음
(sort second [[:a 7] [:c 13] [:b 21]])
;; => Wrong number of args (2) passed to: clojure.core/second--5457

;; 위의 second를 사용하기 위해서는 sort-by를 이용한다.
(sort-by second [[:a 7] [:c 13] [:b 21]])

;; 실패했던 정렬들도 성공 한다.
(sort-by str ["z" "x" "a" "aa" 1 5 8])
(sort-by :age [{:age 99} {:age 13} {:age 7}])

;; 맵의 데이터를 이용하여 정렬하는 예제
(def plays [{:band "Burial" :plays 979 :loved 9}
            {:band "Eno" :plays 2333 :loved 15}
            {:band "Bill Evans" :plays 979 :loved 9}
            {:band "Magma" :plays 2665 :loved 31}])

;; 아래와 같이 partial을 이용하여 원하는 정렬을 조합 할 수 있다.
(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))

(sort-by-loved-ratio plays)

;; 리턴 값으로의 함수
;; 함수를 리턴 한다.
(defn columns [column-names]
  (fn [row]
    (vec (map row column-names))))

;; 조금 신기하게 동작 하는듯
;; map으로 변형후에 그 데이터의 원본 데이터를 알고 있다.
(sort-by (columns [:plays :loved :band]) plays)

(map (columns [:plays :loved :band]) plays)

(sort-by #(vec (map % [:plays :loved :band])) plays)

;; 위 결과를 테스트
(columns [:plays :loved :band])
((columns [:plays :loved :band])
 {:band "Burial" :plays 979 :loved 9})

(map {:band "Burial" :plays 979 :loved 9} [:plays :loved :band])


