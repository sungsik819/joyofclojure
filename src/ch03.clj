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

;; 함수 인자 구조분해
;; 지금까지 구조분해 했던 내용들 전부 사용 가능 하다
(defn print-last-name [{:keys [l-name]}]
  (println l-name))

(print-last-name guy-name-map)

;; repl
(range 5)

(for [x (range 2) y (range 2)] [x y])

(bit-xor 1 2)

(defn xors [max-x max-y]
  (for [x (range max-x) y (range max-y)]
    [x y (rem (bit-xor x y) 256)]))

(xors 2 2)

(def frame (java.awt.Frame.))

;; 클래스의 매소드 확인
(for [meth (.getMethods java.awt.Frame)
      :let [name (.getName meth)]
      :when (re-find #"Vis" name)]
  name)

;; 위에서 찾은 메소드 테스트
(.isVisible frame)

(.setVisible frame true)

;; 화면 사이즈 조절
(.setSize frame (java.awt.Dimension. 200 200))

;; 그림 그리기 위한 컨텍스트 가져오기
(def gfx (.getGraphics frame))

;; 화면 클리어 하기
(defn clear [g] (.clearRect g 0 0 200 200))

(clear gfx)

;; 사각형 그리기
(.fillRect gfx 100 100 50 75)


;; 색 변경 후 다른 사각형 그리기
(.setColor gfx (java.awt.Color. 255 128 0))
(.fillRect gfx 100 150 75 50)

;; 이를 바탕으로 화면에 그림 그리기
(doseq [[x y xor] (xors 200 200)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))

;; 예외 발생 시키기
(doseq [[x y xor] (xors 500 500)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))

;; 발생된 예외의 상세 정보 확인 하기
(.printStackTrace *e)

;; xors에서 함수를 받아서 처리하는 버전
(defn f-values [f xs ys]
  (for [x (range xs) y (range ys)]
    [x y (rem (f x y) 256)]))

;; 특정 함수를 받아서 화면에 그리는 버전
(defn draw-values [f xs ys]
  (clear gfx)
  (.setSize frame (java.awt.Dimension. xs ys))
  (doseq [[x y v] (f-values f xs ys)]
    (.setColor gfx (java.awt.Color. v v v))
    (.fillRect gfx x y 1 1)))
