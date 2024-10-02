(ns joy.event-sourcing
  (:require [joy.generators :refer [rand-map]]))


;; 간단한 이벤트 소스 모델
;; 상태의 한 스냅샷으로 부터 출발 한다.
;; {:ab 5
;;  :h  2
;;  :avg 0.400}

;; 위 맵은 직접 수정되는 것이 아닌 이벤트로부터 도출 된다.
;; 데이터 테이블과 유사하다.

;; 각 이벤트 들도 다음과 같은 맵으로 구성된다.
;; {:result :hit}

;; 이벤트나 이벤트 시퀀스로부터 정보를 얻으려면 보조 함수 필요

;; 이벤트 구문 점검
(defn valid? [event]
  (boolean (:result event)))

;; 상태를 변경하는 이벤트 소싱 함수
(defn effect [{:keys [ab h] :or {ab 0 h 0}} event]
  (let [ab (inc ab)
        h (if (= :hit (:result event))
            (inc h)
            h)
        avg (double (/ h ab))]
    {:ab ab :h h :avg avg}))

;; 이벤트가 유효한 경우에만 반영하는 함수
(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
    state))

;; 이벤트 소싱과 다수의 이벤트를 반영하는 함수
(def effect-all #(reduce apply-effect %1 %2))

;; 주어진 시점의 상태에 대한 정보 얻기
;; 시간에 따른 이력
(def fx-timeline #(reductions apply-effect %1 %2))

(comment
  (valid? {})

  (valid? {:result 42})

  (effect {} {:result :hit})
  (effect {:ab 599 :h 180} {:result :out})

  (apply-effect {:ab 600 :h 180 :avg 0.3}
                {:result :hit})

  (effect-all {:ab 0 :h 0}
              [{:result :hit}
               {:result :out}
               {:result :hit}
               {:result :out}])

  ;; effect-all을 테스트하여 모델 검증
  (def events (repeatedly 100
                          (fn []
                            (rand-map 1
                                      #(-> :result)
                                      #(if (< (rand-int 10) 3)
                                         :hit
                                         :out)))))

  (effect-all {} events)

  ;; 일부만 반영
  (effect-all {} (take 50 events))

  (fx-timeline {} (take 3 events)))

