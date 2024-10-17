(ns joy.scalar)

;; 스칼라 : 기본 데이터 타입
;; 숫자
;; 정수
;; 10진수, 16진수, 8진수, radix-32, 바이너리 문자
;; 0x7F, 16r7F 둘다 16진수
[127 0x7F 16r7F 0177 32r3V 2r01111111]

;; 부동 소수
[1.17 +1.22 -2. 366e7 32e-14 10.7e-3]

;; 유리수
[22/7 -7/22 2131312321312/21512312312312 -103/4]

;; 심벌
(def yucky-pi 22/7)
yucky-pi

;; 키워드
[:chumby :2 :? :ThisIsThmeNameOfaKeyword]

;; 문자열
"This is a string"
"This is also a 
  String"

;; 문자
;; 유니코드는 \u로 시작한다.
[\a \A \u0042 \\ \u30DE]

;; java.lang.Character로 저장된다.
(class \a)