(ns ch03.joy.repl)

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

(draw-values bit-and 256 256)
(draw-values + 256 256)
(draw-values * 256 256)