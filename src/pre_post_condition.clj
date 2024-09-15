(ns pre-post-condition)

;; 선행 후행 조건으로 함수 제약 하기
(defn slope [p1 p2]
  {:pre [(not= p1 p2) (vector? p1) (vector? p2)]
   :post [(float? %)]}
  (/ (- (p2 1) (p1 1))
     (- (p2 0) (p1 0))))

;; 같은 값이 아니여야 한다.
(slope [10 10] [10 10])

;; 인자가 벡터여야 한다.
(slope [10 1] '(1 20))

;; 리턴 값이 float이어야 한다.
(slope [10 1] [1 20])

(slope [10.0 1] [1 20])

;; 함수로부터 선언 분리 하기
(defn put-things [m]
  (into m {:meat "beef" :veggie "broccoli"}))

(put-things {})

;; 위 함수의 제약 조건을 추가 하기 위해서는 함수를 수정 하는 것 보다는
;; 함수를 추가하여 검증 하도록 하자
(defn vegan-constraints [f m]
  {:pre [(:veggie m)]
   :post [(:veggie %) (nil? (:meat %))]}
  (f m))

;; 리턴 값에 :meat가 없어야 한다.
(vegan-constraints put-things {:veggie "carrot"})

;; 육류와 채소를 포함하고 있다.
(defn balanced-diet [f m]
  {:post [(:meat %) (:veggie %)]}
  (f m))

(balanced-diet put-things {})

;; 육류는 변경하지 않는다.
(defn fincky [f m]
  {:post [(= (:meat %) (:meat m))]}
  (f m))

(fincky put-things {:meat "chicken"})


