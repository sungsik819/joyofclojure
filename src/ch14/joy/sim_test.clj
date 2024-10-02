(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer [rand-map]]
            [clojure.set :as sql]))

;; 값 중심의 프로그램 구성 방법
;; 시뮬레이션은
;; 동작을 입력 하고
;; 데이터로 표현 하고
;; 시스템이나 라이브러리의 상호작용을 이끌어내고
;; 하위 시스템을 동작시키며
;; 모델을 로딩 하는 행위

;; 예제 라이브러리를 사용해서
;; 야구 능력에 대한 간단한 모델을 동작시켜 얻을 수 있는 통계 셋을 생성

;; 야구 선수들의 능력을 표현하는 데이터 모델
(def PLAYERS #{{:player "Nick", :ability 32/100}
               {:player "Matt", :ability 26/100}
               {:player "Ryan", :ability 19/100}})

(defn lookup [db name]
  (first (sql/select
          #(= name (:player %))
          db)))



;; 데이터베이스에 야구 결과 이벤트 반영하기
(defn update-stats [db event]
  (let [player (lookup db (:player event))
        less-db (sql/difference db #{player})]
    (conj less-db
          (merge player (es/effect player event)))))




;; 데이터 저장소에 결과 이벤트들을 트랜잭션 방식으로 반영하기
(defn commit-event [db event]
  (dosync (alter db update-stats event)))

;; 선수 능력에 따른 랜덤 야구 이벤트 생성하기
(defn rand-event [{ability :ability}]
  (let [able (numerator ability)
        max (denominator ability)]
    (rand-map 1
              #(-> :result)
              #(if (< (rand-int max) able)
                 :hit
                 :out))))

;; 다수의 랜덤 야구 이벤트 생성하기
(defn rand-events [total player]
  (take total
        (repeatedly #(assoc (rand-event player)
                            :player
                            (:player player)))))

;; 선수명에 해당하는 에이전트 검색 또는 생성
(def agent-for-player
  (memoize
   (fn [player-name]
     (let [a (agent [])]
       (set-error-handler! a #(println "ERROR: " %1 %2))
       (set-error-mode! a :fail)
       a))))

;; 데이터 저장소와 선수 이벤트 저장소에 이벤트 공급하기
(defn feed [db event]
  (let [a (agent-for-player (:player event))]
    (send a
          (fn [state]
            (commit-event db event)
            (conj state event)))))

;; 데이터 저장소와 선수 이벤트 저장소에 전체 이벤트 공급하기
(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)

;; 시뮬레이션 드라이버
(defn simulate [total players]
  (let [events (apply interleave
                      (for [player players]
                        (rand-events total player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %)) players))
    @results))

;; 본질적 상태 와 파생된 상태
;; 본질적 상태 - 원시 데이터를 말한다. 여기서는 :ab, :h 데이터
;; 파생된 상태 - 본질 데이터가 계산된 데이터, 여기서는 :avg


(comment
  (lookup PLAYERS "Nick")

  (update-stats PLAYERS {:player "Nick" :result :hit})

  (commit-event (ref PLAYERS) {:player "Nick" :result :hit})

  (rand-events 3 {:player "Nick" :ability 32/100})

  (let [db (ref PLAYERS)]
    (feed-all db (rand-events 100 {:player "Nick" :ability 32/100})))

  ;; 100개가 생성 되었는지 확인
  (count @(agent-for-player "Nick"))

  ;; :result keyword가 있기 때문에 실행이 가능 하다.
  (es/effect-all {} @(agent-for-player "Nick"))

  (simulate 2 PLAYERS)

  (simulate 400 PLAYERS)

  (es/effect-all {} @(agent-for-player "Nick")))