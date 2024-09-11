(ns regex)

;; 정규식 문법
#"an example pattern"

(class #"example") ;; => java.util.regex.Pattern

;; 자바 클래스로 사용하는 경우 아래와 같이 사용함
(java.util.regex.Pattern/compile "\\d")

;; 정규식 함수
;; 문자열 찾기
(re-seq #"\w+" "one-tow/three")

;; 그룹 지정
(re-seq #"\w*(\w)" "one-two/three")

;; 가변적 Matcher 유의해서 사용 해야함