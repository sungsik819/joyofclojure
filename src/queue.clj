(ns queue)

;; 출력 메소드 재정의
(defmethod print-method clojure.lang.PersistentQueue
  [q w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

;; 비어있는 큐
clojure.lang.PersistentQueue/EMPTY

;; 비어있는 큐에서 pop
;; 빈 큐 리턴함
(pop clojure.lang.PersistentQueue/EMPTY)

;; 비어있는 큐에서 nil 리턴
(peek clojure.lang.PersistentQueue/EMPTY)

(def schedule
  (conj clojure.lang.PersistentQueue/EMPTY :wake-up :shower :brush-teeth))

schedule

;; 큐는 시퀀스와 벡터가 혼합되어 있다.
;; peek시에는 시퀀스의 앞쪽 항목 리턴
;; pop시에는 앞의 항목을 제외한 시퀀스를 리턴
;; conj는 뒤쪽의 벡터에 새 항목을 추가

;; 꺼내기
(peek schedule)

;; 삭제하기
;; pop은 queue를 리턴 한다.
(pop schedule)

;; rest는 seq를 리턴 한다.
(rest schedule)

;; queue를 rest로 사용 후 conj하면?
;; seq이기 때문에 앞에 추가 된다.
(conj (rest schedule) 1)
