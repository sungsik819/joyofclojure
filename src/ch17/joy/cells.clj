(ns joy.cells)

;; 보이지 않는 디자인 패턴
(defmacro defformula [nm bindings & formula]
  `(let ~bindings
     (let [formula# (agent ~@formula)
           update-fn# (fn [key# ref# o# n#]
                        (send formula# (fn [_#] ~@formula)))]
       (doseq [r# ~(vec (map bindings
                             (range 0 (count bindings) 2)))]
         (add-watch r# :update-formula update-fn#))
       (def ~nm formula#))))

(def h (ref 25))
(def ab (ref 100))

;; 타율 계산에 defformula 사용하기
(defformula avg
  [at-bats ab hits h]
  (float (/ @hits @at-bats)))

@avg

(dosync (ref-set h 33))

@avg