(ns joy.nil)

;; nil 과 false 비교
(when (nil? nil) "Actually nil, not false")

;; nil의 중요성
(seq [1 2 3]) ;; => (1 2 3)

;; 빈 리스트 true 이므로 false로 하기 위해서는 nil로 바꾸는 방법을 사용 한다.
(seq []) ;; => nil

;; rest는 비어있어도 시퀀스를 리턴 한다.
(defn print-seq [s]
  (when (seq s) (prn (first s))
        (recur (rest s))))

(print-seq [])

(print-seq [1 2])