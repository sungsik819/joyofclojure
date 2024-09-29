(ns basic-interface
  (:import [java.util Comparator Collections ArrayList]
           [java.util.concurrent FutureTask]))

;; 모든 클로저 함수들이 구현하고 있는 인터페이스

;; 모든 클로저 함수들이 구현 하고 있는 인터페이스 확인
(ancestors (class #()))

;; java.util.comparator 인터페이스
(defn gimme []
  (ArrayList. [1 3 4 8 2]))

(doto (gimme)
  (Collections/sort (Collections/reverseOrder)))

;; 자바로 구현한 방법
(doto (gimme)
  (Collections/sort
   (reify Comparator
     (compare [this l r]
       (cond
         (> l r) -1
         (= l r) 0
         :else 1)))))

;; 더 나은 방법
(doto (gimme)
  (Collections/sort #(compare %2 %1)))

(doto (gimme)
  (Collections/sort >))

(doto (gimme)
  (Collections/sort <))

;; 반대의 결과를 리턴하는 함수
(doto (gimme)
  (Collections/sort (complement <)))

;; java.lang.Runnable 인터페이스
;; 자바의 스레드는 java.lang.Runnable 인터페이스를 구현
;; 처리 입장에서는 리턴하는 값이 없다는 의미가 된다.
;; 함수를 다른 자바 스레드에 전달 하고 싶다면 Thread 생성자에
;; 인자로 전달하기만 하면 된다.
;; 모든 클로저 함수들이 java.lang.Runnable을 구현하기 때문이다.
(doto (Thread. #(do (Thread/sleep 5000)
                    (println "haikeeha!")))
  .start)

;; java.util.concurrent.Callable 인터페이스
;; 값을 리턴하는 연산에 대한 스레드 처리에 사용된다.
;; 이때는 자바의 java.util.concurrent.FutureTask 클래스를 사용하는
;; 클로저 함수가 사용되는데, 이 클래스는 '나중에 처리되는 연산'을 표현한다.
;; 완료 될 때까지 멈춰 있다가 완료되면 get이 실해 된다.
(let [f (FutureTask. #(do (Thread/sleep 5000) 42))]
  (.start (Thread. #(.run f)))
  (.get f))