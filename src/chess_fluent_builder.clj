(ns chess-fluent-builder)

;; 자바코드는 책 참고 P.276

;; 클로저 코드
;; 아래 컬렉션 구조로 구현 된다.
;; {:from "Q7" :to "e8" :castle? false :promotion \Q}

;; DSL 인가?
(defn build-move [& pieces]
  (apply hash-map pieces))

(build-move :from "e7" :to "e8" :promotion \Q)

;; 콘솔, 로그 파일에 출력 적용하기 위한 레코드 사용
;; Object의 toString을 오버라이드 한다.
(defrecord Move [from to castle? promotion]
  Object
  (toString [this]
    (str "Move " (:from this)
         " to " (:to this)
         (if (:castle? this) " castle"
             (if-let [p (:promotion this)]
               (str " promote to " p)
               "")))))

(str (Move. "e2" "e4" nil nil))
;; (println (Move. "e7" "e8" nil \Q))
(.println System/out (Move. "e7" "e8" nil \Q))

;; 관심사의 분리
;; 자바에서는 한개의 클래스가 너무 여러가지 일을 하면 확장하기 어렵다.
;; 만일 체스 게임의 다른 규칙들을 포함하려면 중복된 체스 규칙들이 여기저기 흩어져 있게 된다.
;; 이동 구조를 하나의 값으로 보게 되면 클로저 코드로는 전체 솔루션을 쉽게 구현 할 수 있다.

;; build-move 함수에서 Move 생성자를 랩핑하여 타입 자체에 규칙을 포함하지 않는다.
;; 선행 조건을 사용하여 필수적인 필드를 지정 한다.
;; 인자에 이름을 부여하여 구조분해를 함으로써 입력 순서 문제도 해결 된다.
;; 생성된 레코드는 맵이기 때문에 맵이 사용되는 대부분의 상황에서도 동작 된다.
(defn build-move [& {:keys [from to castle? promotion]}]
  {:pre [from to]}
  (Move. from to castle? promotion))

(str (build-move :from "e2" :to "e4"))

;; to가 없으므로 실패 한다.
(build-move :from "e2")

