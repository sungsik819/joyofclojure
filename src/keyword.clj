(ns keyword)

;; 키워드의 용도
;; 키워드는 자기 자신을 가지고 심벌은 그렇지 않다.
;; :magma는 항상 그 값을 :magma로 갖고
;; 심벌 ruins는 클로저의 다른 값이나 참조를 가리킬 수도 있다.
:a-keyword
::also-a-keyword

;; 키로 활용
(def population {:zombies 2700 :humans 9})

(get population :zombies)

(println (/ (get population :zombies)
            (get population :humans))
         "zombies per capita")

;; 함수로 활용
(:zombies population)

(println (/ (:zombies population)
            (:humans population))
         "zombies per capita")

;; 지시자로 활용
;; :else
(defn pour [lb ub]
  (cond
    (= ub :toujours) (iterate inc lb)
    :else (range lb ub)))

(pour 1 10)

;; (pour 1 :toujoure) => 영원히 반복됨 

::keyword

;; (ns another)

;; 위 another ns에서 해도 아래는 특정 네임스페이스에 속한 키워드가 아님
:keyword/namespace

;; 도메인에서 배관 분리 하기
(defn do-blowfish [directive]
  (case directive
    :aquarium/blowfish (println "feed the fish")
    :crypto/blowfish (println "encode the message")
    :blowfish (println "not sure what to do")))

;; (ns crypto) ;; 주석 해지해야함
;; crypto에 속하지 않음
;; :blowfish
(keyword/do-blowfish :blowfish)

;; crypto에 속함
;; :crypto/blowfish
(keyword/do-blowfish ::blowfish)

;; (ns aquarium) ;; 주석 해지 해야함
;; aquarium에 속하지 않음
;; :blowfish
(keyword/do-blowfish :blowfish)

;; aquarium에 속함
;; :aquarium/blowfish
(keyword/do-blowfish ::blowfish)