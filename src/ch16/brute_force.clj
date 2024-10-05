(ns brute-force
  (:require [clojure.set :as set]
            [utils :refer [pos]]))

;; 검색 문제
;; 무차별적 스도쿠 풀이

;; 스도쿠의 모양
;; 전체는 9x9 크기의 정사각형이다.
;; 이 정사각형은 3x3 크기의 9개 정사각형들로 나뉜다.
;; 드문드문 숫자가 포함된 공간들이 포함되어 있다.

;; 조건
;; 각 열은 1에서 9까지의 숫자들을 한 번씩만 포함해야 한다.
;; 각 행은 1에서 9까지의 숫자들을 한 번씩만 포함해야 한다.
;; 각 3x3 크기 정사각형들은 1에서 9까지의 숫자들을 한 번씩만 포함해야 한다.

;; 보드
(def b1 '[3 - - - - 5 - 1 -
          - 7 - - - 6 - 3 -
          1 - - - 9 - - - -
          7 - 8 - - - - 9 -
          9 - - 4 - 8 - - 2
          - 6 - - - - 5 - 1
          - - - - 4 - - - 6
          - 4 - 7 - - - 2 -
          - 2 - 6 - - - - 3])

(defn prep [board]
  (map #(partition 3 %)
       (partition 9 board)))

;; 스도쿠 보드 시작 상태 출력하기
(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "  ")))
        (print (str (last subrow) " | ")))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

(-> b1 prep print-board)

;; 보드 상의 각 칸들에 대한 고려 사항
;; 해당 칸을 포함하는 행
;; 해당 칸을 포함하는 열
;; 해당 칸을 포함하는 3x3 크기의 서브 그리드

(defn rows [board sz]
  (partition sz board))

(defn row-for [board index sz]
  (nth (rows board sz) (/ index 9)))

(row-for b1 1 9)

(defn column-for [board index sz]
  (let [col (mod index sz)]
    (map #(nth % col)
         (rows board sz))))

(column-for b1 0 9)

(defn subgrid-for [board i]
  (let [rows (rows board 9)
        sgcol (/ (mod i 9) 3)
        sgrow (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) sgcol 3)
        grp (take 3 (drop (* 3 (int sgrow)) grp-col))]
    (flatten grp)))

(subgrid-for b1 0)

;; 스도쿠 규칙
;; 문제 해결을 위한 의사 코드
;; 첫번째 빈 사각형에 숫자를 배치한다.
;; 제약 조건에 부합하는지 확인 한다.
;; - 부합하면 이 알고리즘을 다시 시작한다.
;; - 부합하지 않으면 그 숫자를 제거하고, 이 알고리즘을 다시 시작한다.
;; 반복한다.

;; 현재 배치된 숫자 확인
(defn numbers-present-for [board i]
  (set
   (concat (row-for board i 9)
           (column-for board i 9)
           (subgrid-for board i))))

(numbers-present-for b1 1)

;; assoc로 결합하여 사용
(numbers-present-for (assoc b1 1 8) 1)

;; 포함되지 않은 숫자만 가져오기
(defn possible-placements [board index]
  (set/difference #{1 2 3 4 5 6 7 8 9}
                  (numbers-present-for board index)))

;; 무차별적 스도쿠 풀이 함수
(defn solve [board]
  (if-let [[i & _]
           (and (some '#{-} board)
                (pos '#{-} board))]
    (flatten (map #(solve (assoc board i %))
                  (possible-placements board i)))
    board))

(-> b1
    solve
    prep
    print-board)