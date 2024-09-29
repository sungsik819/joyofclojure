(ns array)

;; 배열의 타입 : 기본형과 참조형
;; 참조형 배열이 주어지면 기본형 배열을 전달 할 수 있는 방법이 없다.

;; 기본 타입 배열 생성
;; 정상적으로 동작하지 않는다.
;; into-array 함수가 char[]를 리턴하지 않고,
;; 클로저 컴파일러에 의해 StringBuilder.append(Object) 메서드가 강제 호출 되어
;; 참조형 배열 Character[]를 리턴 한다. 
(doto (StringBuilder. "abc")
  (.append (into-array [\x \y \z])))

;; 위 예제를 정상 동작하려면 기본형 배열을 사용하는 것이다.
(doto (StringBuilder. "abc")
  (.append (char-array [\x \y \z])))

;; 기본형 배열의 종류
;; boolean-array
;; byte-array
;; char-array
;; double-array
;; float-array
;; int-array
;; long-array
;; object-array

;; 기본형 배열 생성시 make-array, into-array 함수도 사용 가능하다
(let [ary (make-array Long/TYPE 3 3)]
  (dotimes [i 3]
    (dotimes [j 3]
      (aset ary i j (+ i j))))
  (map seq ary))

(into-array Integer/TYPE [1 2 3])

;; 참조형 배열 생성
;; 호환 가능한 타입들을 의도적으로 생성하려면 객체의 시퀀스를 into-array 함수에 전달 한다.
(into-array ["a" "b" "c"])

(into-array [(java.util.Date.) (java.sql.Time. 0)])

;; 타입이 맞지 않아서 예외 발생
;; 첫번째 요소를 기준으로 타입이 결정되기 때문에 다음 요소가
;; 호환 가능한 타입(자식 클래스)가 아니면 예외가 발생 된다.
(into-array ["a" "b" 1M])

;; 중간에 타입을 넣어줘서 예외가 발생하지 않는다.
(into-array Number [1 2.0 3M 4/5])

;; Object의 다양한 타입들을 사용해서 배열을 생성하려면
;; to-array, to-array-2d를 사용한다.
;; 단, 기본형들이 오토박싱 된다.
(to-array [[1 2 3]
           [4 5 6]])
(to-array ["a" 1M #(%) (proxy [Object] [])])
(to-array [1 (int 2)])

;; 배열의 가변성
;; JVM 배열들은 가변적이기 때문에 내용이 변경 가능하다.
(def ary (into-array [1 2 3]))
(def sary (seq ary))

;; ary를 변경 하면 
(aset ary 0 42)
;; 참조하고 있는 부분들도 같이 변경 된다.
sary

;; 그러므로 스레드 간의 공유에서는 더욱 조심 해야 한다.
;; asum-sq 처리중에는 어떤 때라도 사용되는 배열이 변경될 가능성이 있다.
;; 배열의 시퀀스(seq 함수로 생성되는)를 공유 할 때는 그 시퀀스(seq)의 참조를 획득하지 않는 한 
;; 배열을 변경할 방법이 없기 때문에 매우 안전하다.
(defn asum-sq [xs]
  (let [dbl (amap xs i ret
                  (* (aget xs i)
                     (aget xs i)))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))

(asum-sq (double-array [1 2 3 4 5]))

;; 이름 부여
(defmulti what-is class)
(defmethod what-is
  (Class/forName "[Ljava.lang.String;")
  [_]
  "1d String")

(defmethod what-is
  (Class/forName "[[Ljava.lang.Object;")
  [_]
  "2d Object")

(defmethod what-is
  (Class/forName "[[[[I")
  [_]
  "Primitive 4d int")

(what-is (into-array ["a" "b"]))
(what-is (to-array-2d [[1 2] [3 4]]))
(what-is (make-array Integer/TYPE 2 2 2 2))

;; 다차원 배열
;; 오류 발생
(what-is (into-array [[1.0] [2.0]]))

(defmethod what-is
  (Class/forName "[[D")
  [_]
  "Primitive 2d double")

(defmethod what-is
  (Class/forName "[Lclojure.lang.PersistentVector;")
  [_]
  "1d Persistent Vector")

;; 위 함수를 정의한 후에 실행하면 vector가 된다.
(what-is (into-array [[1.0] [2.0]]))

;; 이렇게 해야 원하는 결과가 나온다
(what-is (into-array (map double-array [[1.0] [2.0]])))

;; 가변 인자 메서드/생성자 호출
;; String format 예제
(String/format "An int %d and a String %s"
               (to-array [99 "luftballons"]))