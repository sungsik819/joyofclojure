(ns joy.patterns.app
  (:require [joy.patterns.di :as di]))

;; 환경설정으로 시스템 구성하기
(def config {:type :mock :lib 'joy.patterns.mock})