(ns var)

;; var는 네임스페이스 안에서 명명하고 한정할 수 있다.
;; 동적 var는 스레드-로컬 상태를 제공할 수 있다.

;; var은 이름을 평가하면 값을 주고,
;; var 객체가 필요하면 특수 연산자 var을 함께 붙여서 전달 한다.

*read-eval*

;; 두개는 같은 의미 
(var *read-eval*) ;; => #'clojure.core/*read-eval*
#'*read-eval*

;; 바인딩 매크로
(defn print-read-eval []
  (println "*read-eval* is currently" *read-eval*))

(defn binding-play []
  (print-read-eval)
  (binding [*read-eval* false]
    (print-read-eval))
  (print-read-eval))

;; binding된 부분만 false이고 나머지는 true 이다.
(binding-play)

;; 명명된 var 생성하기
;; defn - var에 함수 할당
;; defmacro - var에 매크로 할당
;; defonce - 바인딩 되지 앟은 var에 값을 할당
;; defmulti - var에 멀티메서드 할당

(def favorite-color :green)

favorite-color

(var favorite-color)

;; var 상태
(def x)
(resolve 'x) ;; => #'var/x
(bound? #'x) ;; => false
(thread-bound? #'x) ;; => false

(def x1 5)
(resolve 'x1) ;; => #'var/x
(bound? #'x1) ;; => true
(thread-bound? #'x1) ;; => false

;; binding을 사용하기 위해서는 ^:dynamic으로 설정 해야 한다.
(def ^:dynamic x3 5)

(binding [x3 7]
  (println (resolve 'x3) (bound? #'x3) (thread-bound? #'x3))) ;; => #'var/x3 true true

;; def로 정의 되지 않은 상태에서 x4를 확인하면 nil로 표시
(with-local-vars [x4 9]
  (println (resolve 'x4) (bound? #'x4) (thread-bound? #'x4)))


;; 익명의 var 생성히기
(def x 42)
{:outer-var-value x
 :with-locals (with-local-vars [x 9]
                {:local-var x
                 :local-var-value (var-get x)})}

;; 동적 범위


