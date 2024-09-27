(ns agent
  (:require [thread-util :refer [dothreads!]]))

;; 에이전트는 언제 사용하는가?
;; 시간에 따라 변화하는 값을 대상으로 한다.
;; 값에 대한 처리 동작을 큐에 담아두고,
;; 각 행위는 에이전트가 저장하거나 다음 액션에 전달할 새 값을 생산한다.
;; 에이전트의 상태는 시간에 따라 동작의 흐름에 따라 변화하게 되므로
;; 그 본질상 한 에이전트는 한 시점에 오직 한 개의 액션만을 수행 할 수 있다.
;; 즉, agent는 값 변경이 비동기적이다.
;; 에이전트는 STM 트랜잭션과 결합 되어 있다.

(def joy (agent []))

;; 액션 보내기
(send joy conj "First edition")

;; (deref joy)과 아래는 같은 의미
@joy

(defn slow-conj [coll item]
  (Thread/sleep 1000)
  (conj coll item))

(send joy slow-conj "Second edition")

@joy

;; 인프로세스 모델 vs  분산 병렬 모델 P.303 참고

;; 에이전트로 입출력 제어하기
;; 리소스에 접근 할 때 파일이나 다른 입추력 스트림과 같이 직렬화 한다.
(def log-agent (agent 0))

(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

(defn do-step [channel message]
  (Thread/sleep 1)
  (send-off log-agent do-log (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warning up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "omega")))

(comment
  (all-together-now)
  ;; @로 조회가 가능하다면 agent가 유휴 상태라는 것이다.
  ;; 즉 큐에 입력되거나 실행 중인 액션이 없는 상태다.
  ;; agent가 실행중일 때도 잘 동작 된다.
  @log-agent)

(do
  (do-step "important: " "this must go out")
  @log-agent)

;; await은 log-agent를 사용하는 모든 thread가 종료 된 후에 다음이 실행 된다.
;; 즉, log-agent에 액션이 없으면 다음 액션이 실행 된다.
(do
  (all-together-now)
  (do-step "important: " "this must go out")
  (await log-agent))

;; 코드 컴파일을 다시 하지 않아도 send로 agent에 액션을 보내 두면
(send log-agent (fn [_] 1000))

;; 로직에 의해서 추가된 액션을 수행 한다.
(do-step "epsilon " "near miss")

;; send와 send-off의 차이
;; send는 고정 크기의 스레드 풀에서 그 이상의 액션이 있는 경우 sleep이 끝날때까지 기다렸다가 실행 된다. 
;; CPU 자원을 사용해야하는 업무에 적합하다, 즉 빨리 처리해야하는 로직에 적합하다.

;; send-off는 큐에 들어가고 꺼내면서 스레드에서 실행되는데
;; 무한대 스레드 풀에서 실행 된다. 
;; io 처리에 적합 하다
(defn exercise-agents [send-fn]
  (let [agents (map #(agent %) (range 10))]
    (doseq [a agents]
      (send-fn a (fn [_] (Thread/sleep 1000))))
    (doseq [a agents]
      (await a))))

(time (exercise-agents send-off))
(time (exercise-agents send))

;; 애러 처리
;; :fail 모드
(send log-agent (fn [] 2000))

@log-agent

(agent-error log-agent)

(send log-agent (fn [_] 3000))

;; 처음에 오류가 발생하여 다시 실행 해도 실행되지 않음
@log-agent

;; 아래 코드로 다시 실행 한다.
;; log-agent의 값을 2500으로 리셋
;; 큐에서 대기중이던 모든 액션들을 삭제 한다.
;; :clear-actions true 이 옵션이 없으면 실패만 제외되고
;; 다른 액션들은 지워지지 않고 계속 처리 된다.
;; 에이전트가 실패했을 경우에만 허용 된다.
(restart-agent log-agent 2500 :clear-actions true)

;; restart-agent 실행 후 send, send-off 실행
(send log-agent do-log "The agent, it lives!")

;; agent가 실패하지 않은 상태에서 재시작 하려면
;; 해당 스레드에서는 예외가 발생하게 되고, 에이전트에도 영향이 없다.
;; Execution error at agent/eval10359 (REPL:121).
;; Agent does not need a restart
(restart-agent log-agent 2500 :clear-actions true)

;; :continue 모드
;; agent 생성시 :error-handler를 정의
(defn handle-log-error [the-agent the-err]
  (println "An action sent to the log-agent threw " the-err))

;; error-handler 설정
(set-error-handler! log-agent handle-log-error)

;; continue 모드로 설정
(set-error-mode! log-agent :continue)

;; 일부러 오류를 내본다.
(send log-agent (fn [x] (/ x 0)))

(send log-agent (fn [] 0))

;; 오류가 발생 되더다라도 다른 액션을 보내면 동작 한다.
(send-off log-agent do-log "Stayin' alive, stayin' alive....")

;; 애러 핸들러가 에이전트의 상태를 변경 할 수 없다.
;; 애러 핸들러는 :fail 오류 모드에서도 지원하지만,
;; 핸들러는 restart-agent를 호출할 수 없고, :continue 애러 모드에서보다는
;; 그 유용성이 떨어진다.

;; agent를 사용하지 않아야 하는 경우

;; agent보다 더 나은 메커니즘이 존재하는 경우
;; 열심히 작업중인 스레드 뭉치가 필요한 경우
;; 이벤트 폴링이나 블록킹을 위해 틀별히 길게 실행되는 스레드가 있는 경우
;; 에이전트가 값을 관리하는 것이 적당해 보이지 않는 경우
;; 이럴 때는 자바의 Thread를 직접 사용하거나,
;; dothreads! 처럼 스레드 풀 사용을 고려하거나,
;; 경우에 따라서는 클로저의 future가 적절 할 수도 있다.

;; 상태는 갖지만 에이전트의 액션이 완료되기 전까지는 액션을 보낸 스레드가 진행되지 않도록 해야하는 경우
;; await을 사용하면 되지만 이런 방법은 가능한 피해야 하는 편법이다.