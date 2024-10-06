(ns joy.futures
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip])
  (:import [java.util.regex Pattern]))

;; 콜백으로서의 퓨처
;; XML 피드를 XML zipper로 변환하기
(defn feed->zipper [uri-str]
  (->> (xml/parse uri-str)
       zip/xml-zip))


;; RSS와 Atom 피드 엔트리 일반화하기
(defn normalize [feed]
  (if (= :feed (:tag (first feed)))
    feed
    (zip/down feed)))

(defn feed-children [uri-str]
  (->> uri-str
       feed->zipper
       normalize
       zip/children
       (filter (comp #{:item :entry} :tag))))


;; 일반화된 피드 구조에서 제목 조회하기
(defn title [entry]
  (some->> entry
           :content
           (some #(when (= :title (:tag %)) %))
           :content
           first))

;; 작업을 퓨처 시퀀스에 직접 분산시키기
(def feeds #{"https://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"})

(defn count-text-task [extractor txt feed]
  (let [items (feed-children feed)
        re (Pattern/compile (str "(?i)" txt))]
    (->> items
         (map extractor)
         (mapcat #(re-seq re %))
         count)))

(let [results (for [feed feeds]
                (future
                  (count-text-task title "Elixir" feed)))]
  (reduce + (map deref results)))

;; 퓨처 시퀀ㄴ스를 디스패치하는 매크로
(defmacro as-futures [[a args] & body]
  (let [parts (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task] parts]
    `(let [~res (for [~a ~args] (future ~@acts))]
       ~@task)))


;; 피드 제목에서 문자열 출현 빈도를 병렬적으로 계수하기
(defn occurrences [extractor tag & feeds]
  (as-futures [feed feeds]
              (count-text-task extractor tag feed)
              :as results
              =>
              (reduce + (map deref results))))