(ns planets
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.pldb :as pldb]))

(pldb/db-rel orbits orbital body)

(def facts
  (pldb/db
   [orbits :mercury :sun]
   [orbits :venus :sun]
   [orbits :earth :sun]
   [orbits :mars :sun]
   [orbits :jupiter :sun]
   [orbits :saturn :sun]
   [orbits :uranus :sun]
   [orbits :neptune :sun]))

;; orbits 관계 질의 하기

;; 공전하는 행성 질의하기
(pldb/with-db facts
  (logic/run* [q]
              (logic/fresh [orbital body]
                           (orbits orbital body)
                           (logic/== q orbital))))