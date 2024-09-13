(ns set)

;; 셋은 매치되는 요소 또는 nil을 리턴하는 함수 용도로 가능하다.
(#{:a :b :c :d} :c)

(#{:a :b :c :d} :e)

;; get으로도 가져올 수 있다.
(get #{:a 1 :b 2} :b)

;; get은 해당 요소가 없을 경우의 메시지를 추가할 수 있다. 
(get #{:a 1 :b 2} :z :nothing-doing)

;; 무슨 말일까?
(into #{[]} [()])

(into #{[1 2]} '[(1 2)])

(into #{[] #{} {}} [()])

;; some 사용
;; 벡터에서 contains? 함수 대신 사용할 수 있는 검증 문구
(some #{:b} [:a 1 :b 2])

(some #{1 :b} [:a 1 :b 2])

;; set 순서 유지 하기
(sorted-set :b :c :a)
(sorted-set [3 4] [1 2])

;; 서로 비교가 가능할때만 sorted-set으로 만들 수 있다.
(sorted-set :b 2 :c :a 3 1)

;; 아래와 같은 경우에는 타입이 다르기 때문에 오류가 발생 한다.
(def my-set (sorted-set :a :b))
(conj my-set "a")

;; contains? 함수 사용
;; coll 안에 key 가 존재 해야만 사용이 가능 하다.
(contains? #{1 2 4 3} 4) ;; => true
(contains? [1 2 4 3] 4) ;; => false

;; key를 확인 하는 것이기 때문에 아래 경우에도 참이다.
(contains? {:a 1 :b 2 :c 3} :a)

;; clojure.set
(require 'clojure.set)

;; 또는 네임스페이스에 설정
;; (ns set 
;; (:require clojure.set))

;; 교집합
(clojure.set/intersection #{:humans :fruite-bats :zombies}
                          #{:chupacabra :zombies :humans})

(clojure.set/intersection #{:pez :gum :dots :skor}
                          #{:pez :skor :pocky}
                          #{:pocky :gum :skor})

;; 합집합
(clojure.set/union #{:humans :fruite-bats :zombies}
                   #{:chupacabra :zombies :humans})

(clojure.set/union #{:pez :gum :dots :skor}
                   #{:pez :skor :pocky}
                   #{:pocky :gum :skor})

;; 차집합
(clojure.set/difference #{1 2 3 4} #{3 4 5 6})


