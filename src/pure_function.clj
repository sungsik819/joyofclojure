(ns pure-function)

;; 순수 함수
;; 같은 인자가 주어졌을 때 항상 같은 효과를 리턴 해야 한다.
;; 부수 효과(side effect)를 일으키지 않아야 한다.

;; 참조 투명성
(def plays [{:band "Burial" :plays 979 :loved 9}
            {:band "Eno" :plays 2333 :loved 15}
            {:band "Bill Evans" :plays 979 :loved 9}
            {:band "Magma" :plays 2665 :loved 31}])

(defn keys-apply [f ks m]
  (let [only (select-keys m ks)]
    (zipmap (keys only)
            (map f (vals only)))))

(keys-apply #(.toUpperCase %) #{:band} (plays 0))

(defn manip-map [f ks m]
  (merge m (keys-apply f ks m)))

(manip-map #(int (/ % 2)) #{:plays :loved} (plays 0))

;; 부수 효과가 있는 함수
;; 아래 함수는 더이상 인자로 값이 결정되지 않는다.
;; plays는 언제든지 변경 될 수 있기 때문에 항상 같은 값을 리턴하지 않는다.
(defn merge-love! [ks]
  (map (partial manip-map #(int (* % 1000)) ks) plays))

(merge-love! [:loved])

;; 부수 효과가 있는 함수는
;; 테스트에서도 같은 값을 기대 할 수가 없다.
;; 메모이제이션이나 대수 처리와 같은 기법을 사용해서 최적화 할 수 없다.



