(ns joy.macros)

;; 문법 인용(`), 비인용(~), 평가 이음 기호(~@)
;; `'~v 패턴을 사용해서 런타임에 바인딩 되는 값을 모아둔다.
(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)] ;; 컴파일타임에 let 바인딩 생성
      ~expr)))