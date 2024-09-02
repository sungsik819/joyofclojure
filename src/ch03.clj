(ns ch03)

;; 참
;; nil과 false를 제외하면 모든 것은 참이다.
(if true :truthy :falsey) ;; => :truthy
(if [] :truthy :falsey) ;; => :truthy
(if nil :truthy :falsey) ;; => :falsey
(if false :truthy :falsey) ;; => :falsey

;; 불리언 객체 생성 하지 말것
(def evil-false (Boolean. "false")) ;; 절대 하지 말 것

evil-false ;; => false

(= false evil-false) ;; => true

;; 분기에서는 true 처리 된다.
(if evil-false :truthy :falsey) ;; :truthy

;; 문자열 파싱 하고 싶다면 아래를 사용 하자
(if (Boolean/valueOf "false") :truthy :falsey)

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

;; 구조분해
;; 구조분해 사용하기 전 코드
(def guys-whole-name ["Guy" "Lewis" "Steele"])

(str (nth guys-whole-name 2) ", "
     (nth guys-whole-name 0) " "
     (nth guys-whole-name 1))

;; 백터로 구분
(let [[f-name m-name l-name] guys-whole-name]
  (str l-name ", " f-name " " m-name))

;; 맵으로 구조 분해
(def guy-name-map {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})

(let [{f-name :f-name, m-name :m-name, l-name :l-name} guy-name-map]
  (str l-name ", " f-name " " m-name))

;; keys 사용
(let [{:keys [f-name m-name l-name]} guy-name-map]
  (str l-name ", " f-name " " m-name))

;; :as -> 구조분해 되지 않은 전체 얻기
(let [{f-name :f-name, :as whole-name} guy-name-map]
  (println "First name is" f-name)
  (println "Whole name is below:")
  whole-name)

;; :or를 사용하여 키가 없으면 다른 값 부여
(let [{:keys [title f-name m-name l-name]
       :or {title "Mr."}} guy-name-map]
  (println title f-name m-name l-name))

;; 함수에도 적용 할 수 있다.
(defn while-name [& args]
  (let [{:keys [f-name m-name l-name]} args]
    (str l-name ", " f-name " " m-name)))

(while-name :f-name "Guy" :m-name "Lewis" :l-name "Steele")
(while-name {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})

;; 연관 구조 분해
;; 백터의 인덱스를 이용하여 구조 분해 가능
(let [{first-thing 0 last-thing 3} [1 2 3 4]]
  [first-thing last-thing])