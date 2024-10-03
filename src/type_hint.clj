(ns type-hint)

(set! *warn-on-reflection* true)

(comment
  (defn asum-sq ^Double [^floats xs]
    (let [^floats dbl (amap xs i ret
                            (* (aget xs i)
                               (aget xs i)))]
      (areduce dbl i ret 0
               (+ ret (aget dbl i)))))

  (time (dotimes [_ 10000] (asum-sq (float-array [1 2 3 4 5]))))

  (.intValue (asum-sq (float-array [1 2 3 4 5]))))

(defn asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                          (* (aget xs i)
                             (aget xs i)))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))

;; 임의의 객체에 대해서도 타입 힌트가 가능 하다.
(.intValue ^Double (asum-sq (float-array [1 2 3 4 5])))