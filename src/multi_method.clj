(ns multi-method
  (:require [udp :refer [beget get put]]))

;; 멀티메서드의 도움 받기
;; 임의의 디스패치 함수 결과를 기반으로 함수 다형성을 구현하는 방법 제공

(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx [m] (get m :llvm-compiler))

(def clone (partial beget {}))
(def unix {:os ::unix :c-compiler "cc" :home "/home" :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :llvm-compiler "clang")
             (put :home "/Users")))

(compiler unix)
(compiler osx)

;; 상속된 행위를 위한 애드혹 계층
(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(home unix)
(home osx) ;; 이 상태로는 예외 발생 함, osx는 home이 정의 되어 있지 않아서 임

(derive ::osx ::unix) ;; 관계를 설정하고 하면 osx 실행 가능 함
(home osx)

(parents ::osx)
(ancestors ::osx)
(descendants ::unix)
(isa? ::osx ::unix)
(isa? ::unix ::osx)

;; 계층 내 모순 해결하기
;; osx는 bsd, unix를 상속 받고 있다.
(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")

;; Multiple methods in multimethod 'home' match dispatch value: 
;; :multi-method/osx -> :multi-method/unix and :multi-method/bsd, and neither is preferred
(home osx) ;; 어떤 것을 호출 해야할지 모른다.

;; 멀티메서드 home을 ::bsd 보다는 ::unix에 디스패치 하는 것을 더 선호 한다.
(prefer-method home ::unix ::bsd)
(home osx)

;; remove-method를 사용해서 ::bsd 디스패치를 제거 하면
;; ::osx에서의 우선적 조회도 제거 된다.
(remove-method home ::bsd)
(home osx)

(derive (make-hierarchy) ::osx ::unix)











