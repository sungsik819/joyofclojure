(ns thread-util
  (:import java.util.concurrent.Executors))

;; dothreads 함수 예제
;; cpu 개수보다 2개 많은 스레드 풀 생성
(def thread-pool
  (Executors/newFixedThreadPool
   (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(defn dothreads!
  [f & {thread-count :threads
        exec-count :times
        :or {thread-count 1 exec-count 1}}]
  (dotimes [t thread-count]
    (.submit thread-pool
             #(dotimes [_ exec-count] (f)))))
