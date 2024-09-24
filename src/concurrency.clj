(ns concurrency
  (:import java.util.concurrent.Executors)
  (:require [a-star :refer [neighbors]]))

;; 동시성은 설계 자체에만 관심 있다.
;; 클로저 격언 : 가변성이 복잡하게 얽혀있다는 것은 코드를 조금만 변경해도 일이 커질 수 있다는 것을 의미한다.

;; 병행(concurrency) vs 병렬(parallelism)
;; 병행 - 대략적으로 같은 시간에 다른 종류의 작업을 실행하는 것을 의미
;; 각 작업은 공통된 리소스를 공유하지만 서로 관련된 작업을 수행할 필요는 없다.
;; 병행 작업의 결과물은 종종 다른 병행 작업의 동작에 영향을 미치므로 비결정론(nondeterminism)의
;; 요소를 포함 한다고 할 수 있다.

;; 병렬 - 한 작업을 여러부분으로 나누어 동시에 실행하는 것을 의미
;; 일반적으로 병렬작업은 다시 결합되는 것을 목포로 동작
;; 다른 병렬 작업에 영향을 주지 않기 때문에 확정적으로 관리 된다.

;; 소프트웨어 트랜잭션 메모리(STM)
;; 변수값 셀과 관련된 동시성 정보 업데이트를 위한 넌블록킹 방법이다.
;; ref 타입을 통해 STM 구현 내용을 확인

;; ref는 언제 사용하는가?

;; 클로저의 상태 관리와 가변성 모델의 기반
;; 시간(Time) - 이벤트가 발생하는 상대적 시간
;; 상태(State) - 어떤 시점에서 엔티티 속성들의 스냅 샷
;; 동일성(Identity) - 시간에 따른 공통의 상태 흐름 중에서 식별된 논리적 엔티티

;; 클로저 모델에서 프로그램이 동일성을 사용하려면 항상 가장 최신의 것이 아닌
;; 한 시점에서 속성들의 스냅샷을 받는 것
;; 모든 결정은 연속선상에 놓여져 있다.

;; 동일성의 의미를 다루기 위해 ref 참조 타입과 소프트웨어 트랜잭션 메모리에 의해
;; 관리되는 그 의미의 변화와 같은 장치들을 제공 한다.
;; 이들은 애플리케이션의 시간 흐름 속에서 상태의 동시성을 보장해주며, 트랜잭션 된다.

;; dosync 구문으로 표시되는 트랜잭션은 가변적 데이터 셀의 집합을 구성하는데 사용된다.
;; 데이터 셀은 한꺼번에 변경 되거나, 실패하면 모두 변경되지 않는다.
;; db의 트랜잭션과 유사하다.

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

(dothreads! #(.print System/out "Hi ") :threads 2 :times 2)

;; ref를 사용한 가변적 게임 보드 구현
;; 어떻게 관리 되는 것일까?

;; 클로저의 ref를 사용한 3x3 체스 보드 표현
(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])

(defn board-map [f board]
  (vec (map #(vec (for [s %] (f s))) board)))

;; ref 예제 구성하기
(defn reset-board!
  "보드 상태를 리셋한다. 일반적으로는 이러한 함수가 권장되지 않음"
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

;; 킹이 가능한 이동 정의
(def king-moves
  (partial neighbors
           [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]] 3))

(defn good-move? [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  "적절한 이동을 랜덤하게 선택"
  [[[mover mpos] [_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos) ;; 첫번째 가능한 이동 선택
               (shuffle (king-moves mpos)))]) ;; 가능한 이동 목록을 섞음

;; 테스트
(reset-board!)
(take 5 (repeatedly #(choose-move @to-move)))

;; to-move 앞까지 랜덤하게 이동하도록 하는 함수 생성
;; alter를 이용하여 트랜잭션 내에서 ref 업데이트 하기
(defn place [from to] to)

(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece) ;; 이동하는 말을 위치시킴
  (alter (get-in board src) place :-)
  (alter num-moves inc))

;; 새 위치로 교체
(defn update-to-move [move]
  (alter to-move #(vector (second %) move)))

(defn make-move []
  (let [move (choose-move @to-move)]
    (dosync (move-piece move @to-move)) ;; 이 트랜젝션에서는 보드와 num-moves를 업데이트
    (dosync (update-to-move move)))) ;; 이 트랜젝션에서(주의?)는 to-move 업데이트

(reset-board!)
(make-move)
(board-map deref board)
(make-move)

(board-map deref board)

(dothreads! make-move :threads 100 :times 100)

;; 위 코드를 실행 후 확인 해보면 결과가 잘못 되었다.
;; 원인은 to와 from의 ref 업데이트를 make-move 내에서 
;; 두 개의 다른 dosync를 사용해서 다른 트랜잭션으로 분리 했기 때문이다.
(board-map deref board)

;; 트랜잭션
;; p.289 그림에서 트랜잭션 흐름 참고
;; STM은 락이 없다.
;; 스냅샷 격리를 보장하기 위해 멀티버전 동시성 컨트롤(MVCC)를 사용한다.
;; 스냅샷 격리란 각각 고유의 뷰를 사용하는 것을 의미 한다.
;; 각 트랜잭션은 그안에 있는 값들만 변경하면서 진행되고, 
;; 다른 트랜잭션 값은 기억하지 않는다.
;; 흐름의 마지막 부분에서 로컬 값에 충돌이 발생하는지 확인 한다.

;; 각 트랜잭션은 고유의 격리된 스냅 샷을 갖고 있다.
;; 데이터는 커밋이 성공하기 전까지는 수정되지 않기 때문에 재시도에 따른 위험성이 없다.
;; STM 트랜잭션은 추가적인 요소들을 결합 하지 않고도 쉽게 중첩시킬 수 있다.

;; 임베디드 트랜잭션
;; 그림 10.7 참고
;; clojure.b에서 재시작이 발생되면 더 큰 트랜잭션도 재시작 된다.
;; 클로저는 가장 바깥쪽의 큰 트랜잭션만 커밋 할 수 있다.

;; STM 덕택에 편해진 것들
;; 하지만 세상에 공짜 점심은 없다.

;; 일관성 있는 정보
;; 락이 불필요 함
;; ACID - 클로저는 원자성, 일관성, 고립성 제공, 지속성은 메모리이므로 다른 방향으로 위임하길 권장

;; 발생 가능한 문제들
;; 쓰기 왜곡
;; 트랜잭션이 그 동작의 제어를 위해 사용하는 참조 값을 사용하는데
;; 이 참조에 기록하지 못한 경우에 발생 할 수 있다.
;; 바로 이 때 다른 트랜잭션이 동일한 참조 값을 업데이트 하는 경우가 있다.
;; 이를 방지하는 한가지 방법은 첫 번째 트랜잭션에서 더미를 기록해 두는 것이다.
;; 클로저는 좀 더 비용이 적게 드는 ensure 함수를 해결책으로 제시
;; 이런 문제가 빌생하는 경우는 아주 드물지만 가능성은 존재한다.

;; 라이브 락
;; 트랜잭션들끼리 반복적으로 서로 재시작 시키는 경우를 말한다.
;; 라이브 락은 몇가지 트랜잭션 안에 있는 작업 크기가 너무 클 때 발생 한다.
;; 두가지 방법으로 방지
;; 1. 트랜잭션 재시작에 제한을 두어 이 제한을 어기는 경우 에러를 발생 시키기
;; 2. 바징(barging) - 얼마 되지 않은 트랜잭션이 재시도하는 동안 
;; 오래된 트랜잭션이 계속해서 동작하도록 만드는 것

;; STM에서 피해야 하는 것들
;; I/O
;; 입출력 수행이 필요한 경우에는 io! 매크로를 사용 하자 - 진짠가?
(io! (.println System/out "Haikeeba!"))

;; 예외 발생
(dosync (io! (.println System/out "Haikeeba!")))

;; 클래스 인스턴스 변경

;; 큰 트랜잭션
;; 단위 작업을 분할 할때는 항상 가능한 빠르게 들어가서 빠르게 나와야 한다.
;; 코드 상에서 그 사용을 최소화하도록 노력해야 한다.
;; 프로그램을 논리적인 부분들로 잘 나누어서 입출력과 같은 종류의 동작을 한쪽으로,
;; 트랜잭션 처리와 변경은 다른쪽으로 분리하는 것이 중요하다.

;; 클로저는 변경의 관리를 위한 도그들을 제공하지만
;; 어떤 도구도 생각하는 법을 알려주지는 않는다.
;; 멀티스레드 프로그래밍은 어려운 문제다
;; 그 구체적인 내용이 무엇인지와는 상관없이, 
;; 클로저의 상태 관리 도구들이 그러한 문제들을 마법처럼 해결해주는 것은 아니다.

;; ref로 리팩토링 하기
;; 체스보드 예제 리팩토링

;; 현재 체스보드 예제는 보드 상에서 말을 이동하면 어느 편의 차례인지와 상관 없이 업데이트가 일어날 수 있다.
;; 그림 10.8 참고
;; board와 to-move는 서로 종속 관계에 있기 때문에 그들의 상태가 같은 트랜잭션 내에 있도록 설계해야 한다.
;; db도 이렇게 트랜잭션을 관리해야하는게 맞지 않을까?
;; dosync를 하나로 관리 한다.

(defn make-move-v2 []
  (dosync
   (let [move (choose-move @to-move)]
     (move-piece move @to-move)
     (update-to-move move))))

(reset-board!)
(make-move)

(board-map deref board)
@num-moves

;; 수정된 함수가 여러개의 쓰레드에서도 정상적으로 동작 할까?
;; 잘 동작 함
(dothreads! make-move-v2 :threads 100 :times 100)
(board-map #(dosync (deref %)) board)
@to-move
@num-moves

;; commute 값으로 대체하기
;; 주어진 트랜잭션 내에서 참고 값을 완성하는 것이 중요하지 않은 상황이 있을 수도 있다.
;; 참조에 적용할 함수를 인자로 받는 commute 함수 제공
;; 주어진 참조의 동시성 수준을 향상 시키기 위해 최소 두번은 실행 된다.

;; alter를 commute로 교체 하기
(defn move-piece [[piece dest] [[_ src] _]]
  (commute (get-in board dest) place piece)
  (commute (get-in board src) place :-)
  (commute num-moves inc))

(reset-board!)

;; 함수 수정 후 테스트 해보니 잘 실행 됨
(dothreads! make-move-v2 :threads 100 :times 100)
(board-map deref board)
@to-move

(defn update-to-move [move]
  (commute to-move #(vector (second %) move)))

;; update-to-move 함수 수정 후 실행하면 불일치 발생 함
(dothreads! make-move-v2 :threads 100 :times 100)
(board-map #(dosync (deref %)) board)
@to-move