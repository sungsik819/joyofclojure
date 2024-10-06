(ns joy.unit-testing
  (:require [joy.futures :as joy]
            [clojure.test :refer [deftest testing is]]
            [joy.contract :refer [contract]]))

;; 테스트
;; with-redefs 사용하기

(def stubbed-feed-children
  (constantly [{:content [{:tag :title
                           :content ["Stub"]}]}]))

(defn count-feed-entries [url]
  (count (joy/feed-children url)))

(count-feed-entries "http://blog.fogus.me/feed/")

(with-redefs [joy/feed-children stubbed-feed-children]
  (count-feed-entries "dummy url"))

(with-redefs [joy/feed-children stubbed-feed-children]
  (joy/occurrences joy/title "Stub" "a" "b" "c"))

;; 규격으로서의 clojure.test

;; 부분적 규격으로서의 clojure.test
(deftest feed-tests
  (with-redefs [joy/feed-children stubbed-feed-children]
    (testing "Child Counting"
      (is (= 1000 (count-feed-entries "Dummy URL"))))
    (testing "Occurrence Counting"
      (is (= 0 (joy/count-text-task
                joy/title
                "ZOMG"
                "Dummy URL"))))))

;; 계약 프로그래밍

;; 선행 후행 조건 다시 살펴보기
(def sqr (partial
          (contract seq-contract
                    [n]
                    (require (number? n))
                    (ensure (pos? %)))
          #(* % %)))

[(sqr 10) (sqr -9)]

;; sqr에 대한 계약은 다음과 같다.
;; 숫자를 입력해야 하고, 리턴 값은 양수여야 한다.

;; 여러개의 랜덤 값을 던지는 테스트 드라이버
(doseq [n (range Short/MIN_VALUE Short/MAX_VALUE)]
  (try
    (sqr n)
    (catch AssertionError e
      (println "Error on input" n)
      (throw e))))

;; 선행 조건, 후행 조건의 장점
;; 제약 조건은 기대 사항이나 결과에 대한 보증을 기술하기 위한 명세 언어


(comment
  ;; 테스트 실행
  (clojure.test/run-tests 'joy.unit-testing))
