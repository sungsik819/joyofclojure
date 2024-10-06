(ns joy.contract)

;; contract 매크로
;; 함수의 제약 조건을 기술하는 간단한 DSL
;; 1. 이름을 부여 할 수 있어야 한다.
;; 2. 선행/후행 조건을 직관적인 방법으로 기술할 수 있어야 한다.
;; 3. 추후 제약 조건의 추가 적용을 위해 고차 함수로 구성되어야 한다.

;; contract에 대한 구조 만들기
;; 오직 양수만 받아서 2를 곱한 값을 리턴하는 함수에 대한 제약 조건
;; (contract doubler
;;          [x]
;;           (:require
;;            (pos? x))
;;           (:ensure
;;            (= (* 2 x) %)))

;; 최상위 레벨 매크로 contract
(declare collect-bodies)

(defmacro contract [name & forms]
  (list* `fn name (collect-bodies forms)))

;; contract가 리턴할 함수의 형태
;; (fn doubler
;;   ([f x]
;;    {:post [(= (* 2 x) %)]
;;     :pre [(pos? x)]}
;;    (f x)))

;; 여러개의 인자를 받을수 있는 함수 정의 구문을 허용 해야 한다.
;; 함수마다 하나 이상의 제약 조건들을 심벌 벡터로 구분하여 입력 받을수 있도록 해야 한다.
;; 우선은 collect-bodies를 구현
;; 주된 작업은 세 개 단위로 분할되어 있는 contract 몸체의 리스트를 구성 하는 것이다.
;; 분할된 각 요소들은 인자 목록과 contract의 require, ensures를 표현 한다.

(declare build-contract)

(defn collect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))

;; contract의 보조 함수 build-contract
(defn build-contract [c]
  (let [args (first c)]
    (list
     (into '[f] args)
     (apply merge
            (for [con (rest c)]
              (cond (= (first con) 'require)
                    (assoc {} :pre (vec (rest con)))
                    (= (first con) 'ensure)
                    (assoc {} :post (vec (rest con)))
                    :else (throw (Exception.
                                  (str "Unknown tag "
                                       (first con)))))))
     (list* 'f args))))

;; contract 함수와 제약 조건 함수 구성
(def doubler-contract ;; contract 정의
  (contract doubler
            [x]
            (require
             (pos? x))
            (ensure
             (= (* 2 x) %))))