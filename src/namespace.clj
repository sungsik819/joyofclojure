(ns namespace)

(in-ns 'joy.ns) ;; 네임스페이스 생성 및 전환

(def authors ["Chouser"])

(in-ns 'your.ns)

(clojure.core/refer 'joy.ns) ;; 다른 네임스페이스의 모든 정의를 가져옴
joy.ns/authors

(in-ns 'joy.ns) ;; 네임스페이스 전환

(def authors ["Chouser" "Fogus"])

(in-ns 'your.ns)
joy.ns/authors

;; 네임스페이스 생성

;; ns 매크로
;; 네임스페이스가 없으면 생성 한다.
;; java.lang의 모든 클래스, 
;; clojure.core 네임스페이스의 모든 함수 매크로, 특수 구문을 사용 할 수 있다.
;; REPL에서도 사용 가능 하지만 소스 코드 파일에서의 사용을 목적 함
(ns chimp)

(reduce + [1 2 (Integer. 3)])

;; in-ns 함수
;; java.lang 패키지만 로딩 한다.
;; clojure.core는 사용할 수 없다.
(in-ns 'gibbon)

;; Unable to resolve symbol: reduce in this context
(reduce + [1 2 (Integer. 3)])

;; 아래처럼 불러온 후에는 사용 가능 하다.
(clojure.core/refer 'clojure.core)
(reduce + [1 2 (Integer. 3)])

;; create-ns 함수
;; 심벌을 인자로 받아 네임스페이스 객체를 리턴 한다. 
(def b (create-ns 'bonobo))

;; 바인딩 되었는지 확인
((ns-map b) 'String)

;; 바인딩 정보 추가
(intern b 'x 9)
bonobo/x

;; 바인딩 정보 삭제
(ns-unmap b 'x)
bonobo/x

;; create-ns로 생성한 네임스페이스는 바인딩이 가능하기 때문에
;; 아래처럼 바인딩 후 사용 가능 하다.
;; java 클래스 맵핑은 그대로 사용 할 수 있다.
(intern b 'reduce clojure.core/reduce)
(intern b '+ clojure.core/+)

(in-ns 'bonobo)

(reduce + [1 2 3 4 5])

(in-ns 'namespace)
(get (ns-map 'bonobo) 'reduce) ;; 위에서 정의된 reduce 확인

;; reduce 삭제
(ns-unmap 'bonobo 'reduce)

(get (ns-map 'bonobo) 'reduce) ;; 삭제 됐으므로 nil이 표시 된다.

;; 네임스페이스 삭제
(remove-ns 'bonobo)

(all-ns)

;; create-ns, intern을 사용할 때는 이 함수들이 잠재적인 부수 효과를 일으킬 가능성이 있다.
;; 고급 기술이 필요한 경우에 한해서만 사용 하자

;; 필요한 것만 노출 하자
(ns hider.ns)

;; 함수는 defn-로 함수를 숨기는 것이 가능
;; def, defmacro는 def-, defmacro가 없으므로 아래와 같이 메타데이터를 추가 한다.
(defn ^{:private true} answer [] 42)

(ns seeker.ns
  (:refer hider.ns))

(answer)

;; 선언적 포함과 배제



