(ns ch03.joy.destructuring)

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