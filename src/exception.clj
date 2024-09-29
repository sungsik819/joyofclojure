(ns exception)

;; 예외를 주의 하자
;; 클로저 코드를 작성할 때는 계속 진행 할 수 없으면 에러를 사용하고,
;; 진행될 가능성이 있으면 예외를 사용하자

;; 런 타임 vs 컴파일 타임 예외
;; 런 타임 예외
;; 런타임 예외의 두가지 유형 - 애러, 예외
(defn explode [] (explode))
(try
  (explode)
  (catch Exception e "Stack is blown"))

;; StackOverflowError 는 예외가 아닌 Error 이다.
(try
  (explode)
  (catch StackOverflowError e "Stack is blown"))

(try
  (explode)
  (catch Error e "Stack is blown"))


;; 여기부터는 일반적으로 사용하는 부분은 아니다.
(try
  (explode)
  (catch Throwable e "Stack is blown"))

(try
  (throw (RuntimeException.))
  (catch Throwable e "Catching Throwable is Bad"))

;; 컴파일 타임 예외
;; Exception 이긴 하지만 컴파일러에 의해 발생된 것
(defmacro do-something [x] `(~x))
(do-something 1)

;; 오류에 대한 스택 트레이스 확인
(for [e (.getStackTrace *e)] (.getClassName e))

;; 아래처럼 익명 함수로 감싼 상태에서 예외는 매크로는 체크 가능하다.
(comment
  (defmacro pairs [& args]
    (if (even? (count args))
      `(partition 2 '~args)
      (throw (Exception.
              (str "pairs requires an even number of args")))))

  (pairs 1 2 3)
  (pairs 1 2 3 4)
  (fn [] (pairs 1 2 3)))

;; 함수로도 예외 확인이 가능 한데?
;; 익명 함수로 될 경우에는 확인이 되지 않는다.
(comment
  (defn pairs [& args]
    (if (even? (count args))
      (partition 2 args)
      (throw (Exception.
              (str "pairs requires an even number of args")))))

  (pairs 1 2 3)
  (pairs 1 2 3 4)
  (fn [] (pairs 1 2 3)))

;; 예외 처리
;; 여기서는 try ~ catch를 크게 감싼 형태에서 예외를 잡기 때문에
;; 어디서 발생한 예외인지 알기 힘들다.
(defmacro -?> [& forms]
  `(try (-> ~@forms)
        (catch NullPointerException _# nil)))

(-?> 25 Math/sqrt (+ 100))

(-?> 25 Math/sqrt (and nil) (+ 100))

;; 사용자 정의 예외
(defn perform-unclean-act [x y]
  (/ x y))

(try
  (perform-unclean-act 42 0)
  (catch RuntimeException ex
    (println (str "Something went wrong."))))

;; ex-info를 사용한 함수
;; ex-data로 받아서 추가 정보를 보여준다.
(defn perform-cleaner-act [x y]
  (try
    (/ x y)
    (catch ArithmeticException ex-cause
      (throw (ex-info "You attempted an unclean act"
                      {:args [x y]})))))

(try
  (perform-cleaner-act 108 0)
  (catch RuntimeException ex
    (println (str "Received error: " (.getMessage ex)))
    (when-let [ctx (ex-data ex)]
      (println (str "More information: " ctx)))))






