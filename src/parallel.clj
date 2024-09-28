(ns parallel
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [thread-util :refer [dothreads!]])
  (:import [java.util.regex Pattern]))

;; 퓨처는 언제 사용하는가?
;; 계산이 완료되어야 역참조를 통해 값이 전달된다.
;; 처음 역참조하는 시간만큼 걸린다. 아래 코드는 5초정도 걸린다.
(time (let [x (future (do (Thread/sleep 5000) (+ 41 1)))]
        [@x @x]))

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

(defn count-text-task [extractor txt feed]
  (let [items (feed-children feed)
        re (Pattern/compile (str "(?i)" txt))]
    (->> items
         (map extractor)
         (mapcat #(re-seq re %))
         count)))

(count-text-task
 title
 "Erlang"
 "https://feeds.feedburner.com/ElixirLang")


(count-text-task
 title
 "Elixir"
 "https://feeds.feedburner.com/ElixirLang")

;; 작업을 퓨처 시퀀스에 직접 분산시키기
(def feeds #{"https://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"})

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

(occurrences title "released"
             "https://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"
             "https://feeds.feedburner.com/kotlin")

;; 프로미스는 언제 사용 하는가?
;; promise, deliver 조합으로 사용 된다.
;; promise는 연산의 단위
;; deliver는 다른 스레드에 의해서 채워질 갑승ㄹ 대신하는 플레이스홀더 역할
(def x (promise))
(def y (promise))
(def z (promise))

(dothreads! #(deliver z (+ @x @y)))

(dothreads!
 #(do (Thread/sleep 2000)) (deliver x 52))

(dothreads!
 #(do (Thread/sleep 4000)) (deliver y 86))

(time @z)

;; 프로미스로 병렬 작업하기
;; 스레드에 프로미스 시퀀스 디스패치하기
(defmacro with-promises [[n tasks _ as] & body]
  (when as
    `(let [tasks# ~tasks
           n# (count tasks#)
           promises# (take n# (repeatedly promise))]
       (dotimes [i# n#]
         (dothreads!
          (fn []
            (deliver (nth promises# i#)
                     ((nth tasks# i#))))))
       (let [~n tasks#
             ~as promises#]
         ~@body))))

;; with-promises를 사용한 병렬 테스트 실행기
(defrecord TestRun [run passed failed])

(defn pass [] true)
(defn fail [] false)

(defn run-tests [& all-tests]
  (with-promises
    [tests all-tests :as results]
    (into (TestRun. 0 0 0)
          (reduce #(merge-with + %1 %2) {}
                  (for [r results]
                    (if @r
                      {:run 1 :passed 1}
                      {:run 1 :failed 1}))))))

(run-tests pass fail fail fail pass)

;; 콜백 API에서 블록킹 API로
;; 퓨처와 같이 프로미스는 별도의 스레드에서 RPC를 실행할 때 유용하게 사용된다.
;; 여러 개의 RPC 호출을 병렬로 구성할 필요가 있을 때 유용하지만 정반대의 경우도 있음
;; RPC API는 흔히 서비스 호출의 인자와 호출 후에 실행되는 콜백 함수 인자로 받는다.

;; 콜백 기반 호출
(defn feed-items [k feed]
  (k
   (for [item (filter (comp #{:entry :item} :tag)
                      (feed-children feed))]
     (-> item :content first :content))))

(feed-items
 count
 "http://blog.fogus.me/feed/")

;; 블록킹 호출
;; deliver가 실행 될때까지 호출을 차단 한다.
(let [p (promise)]
  (feed-items #(deliver p (count %))
              "http://blog.fogus.me/feed/")
  @p)

;; 아래는 deliver를 이용하는 방법이 아닌 좀 더 일반적인 방법
;; widh-promises를 사용한 병렬 테스트 실행기
(defn cps->fn [f k]
  (fn [& args]
    (let [p (promise)]
      (apply f (fn [x] (deliver p (k x))) args)
      @p)))

(def count-items (cps->fn feed-items count))
(count-items "http://blog.fogus.me/feed/")

;; 결정론적 데드락
;; 프로미스에 값을 전달 하지 않으면 애플리케이션의 데드락을 초래
;; 프로미스의 장점은 프로미스가 데드락에 걸릴 가능성이 결정되어 있다는 점

;; 데드락 재현
(comment
  (def kant (promise))
  (def hume (promise))

  (dothreads!
   #(do (println "Kant has " @kant) (deliver hume :thinking)))

  (dothreads!
   #(do (println "Hume has " @hume) (deliver kant :thinking)))
  ;; 둘중 하나 실행 하면 데드락 됨
  @kant
  @hume)

;; 병렬 연산
;; pvalues 매크로
(pvalues 1 2 (+ 1 2))

;; 리턴 타입이 지연 시퀀스 이므로 접근 비용이 항상 기대한 것은 아니다.
(defn sleeper [s thing]
  (Thread/sleep (* 1000 s)) thing)

(defn pvs []
  (pvalues
   (sleeper 2 :1st)
   (sleeper 3 :2nd)
   (keyword "3rd")))

;; 2초 걸린다.
(-> (pvs)
    first
    time)

;; 마지막 값을 접근 하면 기대한 시간보다 더 걸린다.
;; :3rd에 접근 하려면 3초 정도가 걸린다.
;; 즉, :2nd 시간에 맞춰서 결정 된다.
(-> (pvs)
    last
    time)

;; pmap 함수
;; map의 병렬 버전
;; 매치되는 함수들이 병렬로 실행 된다.
;; pmap의 사용은 상황에 맞춰서 하고, 무분별하게 사용하지 않는다.
(->> [1 2 3]
     (pmap (comp inc (partial sleeper 2)))
     doall
     time)

;; pcalls 함수
;; 인자를 받지 않는 함수를 임의 갯수 만큼 받아서
;; 병렬로 호출 하고, 결과를 지연 시퀀스로 리턴 한다.
;; 역시 트레이드 오프가 되므로 사용하기 전에 이를 고려 해야 한다.
(-> (pcalls
     #(sleeper 2 :1st)
     #(sleeper 3 :2nd)
     #(keyword "3rd"))
    doall
    time)

;; 병렬로 처리한다고 해서 마법처럼 속도개선이 되는 것은 아니다.
;; 병렬 처리로 사용되는 퓨처, 프로미스, pmap, pvalues, pcalls 등과 같은
;; 기능들은 기본적인 기능들이며 이들을 사용해서 필요한 병렬 처리를 구성 한다.

;; 간단한 reduce/fold 소개
;; reducers 라이브러리는 순차적 또는 병렬적 처리를 선택 할 수 있도록 해준다.
(require '[clojure.core.reducers :as r])
(def big-vec (vec (range (* 2000 2000))))

;; 두개가 크게 차이는 안남
(time (reduce + big-vec))
(time (r/fold + big-vec))