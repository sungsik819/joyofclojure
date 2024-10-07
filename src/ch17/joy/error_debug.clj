(ns joy.error-debug
  (:require [clojure.xml :as xml]
            [joy.macros :refer [contextual-eval]]))

;; 에러처리와 디버깅
;; 애러처리

;; 동적 트리 탐색
;; 탐색 할 트리의 구조
;; {:tag <node form> :attrs {} :content {<nodes>}}

(defn traverse [node f]
  (when node
    (f node)
    (doseq [child (:content node)]
      (traverse child f))))

;; traverse의 동작
(traverse {:tag :flower :attrs {:name "Tanpopo"} :content []} println)

;; xml을 구성
(def DB
  (-> "<zoo>
        <pongo>
          <animal>orangutan</animal>
        </pongo>
        <panthera>
          <animal>Spot</animal>
          <animal>lion</animal>
          <animal>Lopshire</animal>
        </panthera>
      </zoo>"
      .getBytes
      (java.io.ByteArrayInputStream.)
      xml/parse))

;; 적절하지 않은 트리 노드를 예외로 처리하기
(defn ^:dynamic handle-weird-animal
  [{[name] :content}]
  (throw (Exception. (str name " must be 'dealt with'"))))

;; 동적 위임 예제
(defmulti visit :tag)

(defmethod visit :animal [{[name] :content :as animal}]
  (case name
    "Spot" (handle-weird-animal animal)
    "Lopshire" (handle-weird-animal animal)
    (println name)))

(defmethod visit :default [node] nil)

(traverse DB visit)

;; 동적 바인딩 사용
(defmulti handle-weird (fn [{[name] :content}] name))

(defmethod handle-weird "Spot" [_]
  (println "Transporting Spot to the circus."))

(defmethod handle-weird "Lopshire" [_]
  (println "Signing Lopshire to a book deal."))

(binding [handle-weird-animal handle-weird]
  (traverse DB visit))

;; 스레드에 특화된 핸들러로도 사용 가능
(def _ (future
         (binding [handle-weird-animal #(println (:content %))]
           (traverse DB visit))))

;; 디버깅
;; println 말고 다른 디버깅 방법도 있음
;; (defn div [n d] (int (/ n d)))
;; 위 함수에서 (div 10 0) 으로 나눌 경우의 오류를 디버깅 한다.
;; (defn div [n d] (break) (int (/ n d))) 를 추가하여 디버깅 한다.

;; 중단점 매크로
;; REPL 리더 오버라이딩하기

;; 일반적인 디버깅 콘솔 리더
(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (if (= input ::tl)
      exit-code
      input)))

(readr #(print "invisible=> ") ::exit)
(readr #(print "invisible=> ") ::exit)

;; REPL 평가 오버라이딩

;; &env를 사용한 로컬 컨텍스트 맵 생성
(defmacro local-context []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym))
                 symbols)
            symbols)))

(local-context)

(let [a 1 b 2 c 3]
  (let [b 200]
    (local-context)))

;; break 정의
(defmacro break []
  `(clojure.main/repl
    :prompt #(println "debug=> ")
    :read readr
    :eval (partial contextual-eval (local-context))))

;; 함수 디버깅
(defn div [n d]
  (break)
  (int (/ n d)))

;; 실행 해보자
(div 10 0)

;; 여러개의 중단점과 매크로 내 중단점
;; keys-apply 함수에 여러개의 중단점 사용하기
(defn keys-apply [f ks m]
  (break)
  (let [only (select-keys m ks)]
    (break)
    (zipmap (keys only) (map f (vals only)))))

(keys-apply inc [:a :b] {:a 1 :b 2 :c 3})

;; 매크로에서 여러 개의 중단점 사용하기
(defmacro awhen [expr & body]
  (break)
  `(let [~'it ~expr]
     (if ~'it
       (do (break) ~@body))))

(awhen [1 2 3] (it 2))


