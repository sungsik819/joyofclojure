(ns symbol)

;; 심벌
;; 같은 이름이라도 별개의 객체로 판단한다.
(identical? 'goat 'goat) ;; => false

;; 이름이 같은지 여부는 아래와 같이 한다.
(= 'goat 'goat) ;; => true

(name 'goat) ;; => "goat"

;; identical? 함수는 심벌이 동일한 객체인 경우에만 true를 리턴 한다.
(let [x 'goat y x]
  (identical? x y)) ;; => true

;; 메타데이터에서 심벌 이름이 같아도 각각의 별개의 메타데이터가 있으므로 동일한 인스턴스가 아니다.
(let [x (with-meta 'goat {:ornery true})
      y (with-meta 'goat {:ornery false})]
  [(= x y)
   (identical? x y)
   (meta x)
   (meta y)]) ;; => [true false {:ornery true} {:ornery false}]


;; 심벌과 네임스페이스
;; 키워드와 마찬가지로 특정 네임스페이스에 속하지 않는다.
;; (ns where-is) ;; 주석 해제후 아래 코드 실행
(def a-symbol 'where-am-i)

a-symbol ;; => where-am-i

;; 평가를 수행할때 심벌이 식별
(resolve 'a-symbol) ;; => where-is/a-symbol

`a-symbol ;; => where-is/a-symbol