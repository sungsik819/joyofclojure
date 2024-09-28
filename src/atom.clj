(ns atom
  (:require [thread-util :refer [dothreads!]]))

;; atom은 언제 사용 하는가?
;; 동기적이면서, 독립적이다.
;; 비교 후 교체(CAS)
;; 한번 설정되면 그것으로 끝이고, 트랜잭션이 재시도 된다고 해도 그 값이 롤백되지 않는다.
;; 부수 효과가 발생한 것이나 마찬가지
;; 트랜잭션에서 사용하려면 그 값의 변경 동작이 여러 번 적용하더라도 
;; 결과가 달라지지 않는(멱등법칙이 성립되는) 경우에 한해서 사용한다.

;; 스레드간 공유
;; atom은 스레드간 공유해서 사용해도 안전하다.
(def ^:dynamic *time* (atom 0))

(defn tick [] (swap! *time* inc))

(dothreads! tick :threads 1000 :times 100)

@*time*

;; 트랜잭션 내에서 atom 사용하기
;; atom 메모이제이션
;; 리셋이 가능한 memoize 함수

(defn manipulable-memoize [function]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply function args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))

(time [(slowly 9) (slowly 9)])

(def sometimes-slowly (manipulable-memoize slowly))

(time [(sometimes-slowly 108) (sometimes-slowly 108)])

(meta sometimes-slowly)




