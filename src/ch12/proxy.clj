(ns ch12.proxy
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]))

;; 프록시
;; 프록시를 사용한다는 것은 
;; 클로저 프로그램 내에 자바의 많은 개념들을 수용하는 것이라는 점을 유념 한다.
(def OK java.net.HttpURLConnection/HTTP_OK)

;; 지금은 항상 200을 리턴
(defn respond
  ([exchange body]
   (respond identity exchange body))
  ([around exchange body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))

;; 자바를 사용하는 가장 단순한 웹 서버
(defn new-server [port path handler]
  (doto
   (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

;; 프록시를 사용한 기본적 웹 핸들러 정의
(defn default-handler [txt]
  (proxy [HttpHandler]
         []
    (handle [exchange]
      (respond exchange txt))))

;; 처음 만들어본 서버
(comment
  (def server
    (new-server
     8123
     "/joy/hello"
     (default-handler "Hello Cleveland")))

  ;; 시작 후 중지
  (.stop server 0))

;; 서버를 재시작 하지 않고 메시지 변경
;; update-proxy를 사용한다.
(comment
  (def p (default-handler
          "There's no problem that can't be solved with another level of indirection"))

  (def server (new-server 8123 "/" p))

  (update-proxy p
                {"handle" (fn [this exchange]
                            (respond exchange (str "this is " this)))})

  ;; 요청 헤더를 출력하는 웹 핸들러
  (def echo-handler
    (fn [_ exchange]
      (let [headers (.getRequestHeaders exchange)]
        (respond exchange (prn-str headers)))))

  (update-proxy p {"handle" echo-handler})

  ;; 현재 프록시의 맵핑 보기
  (proxy-mappings p)

  (.stop server 0))

;; 클로저의 프록시는 주어진 메서드의 바이트 코드를 프로시 클래스에 직접 넣는 것이 아니라
;; 클로저가 적당한 프록시를 생성하고 그 안에 있는 메서드가 메서드의 동작을 구현 한 함수를 바라보도록 하는 것이다.

;; 완전한 프록시
;; OutputStream을 head와 body 태그로 감싸는 around 필터
(defn html-around [o]
  (proxy [FilterOutputStream]
         [o]
    (write [raw-bytes]
      (proxy-super write
                   (.getBytes (str "<html><body>"
                                   (String. raw-bytes)
                                   "</body></html>"))))))

;; 파일명 리스트를 리턴하는 함수
(defn listing [file]
  (-> file
      .list
      sort))

(comment
  (listing (io/file "."))
  (listing (io/file "./README.md")))

;; 파일 목록을 HTML 문자열로 생성하는 간단한 함수
(defn html-links [root filenames]
  (string/join
   (for [file filenames]
     (str "<a href='"
          (str root
               (if (= "/" root)
                 ""
                 File/separator)
               file)
          "'>"
          file "</a><br>"))))

(comment
  (html-links "." (listing (io/file "."))))

;; 파일 크기를 문자열로 표현하는 함수
(defn details [file]
  (str (.getName file) " is "
       (.length file) " bytes."))

(details (io/file "./README.md"))

;; 상대적 경로의 URI를 파일로 변환하는 함수
(defn uri->file [root uri]
  (->> uri
       str
       URLDecoder/decode
       (str root)
       io/file))

(comment
  (uri->file "." (URI. "/README.md"))
  (details (uri->file "." (URI. "/README.md"))))

;; 로컬 파일 시스템을 탐색하고 목록을 만드는 웹 핸들러
(def fs-handler
  (fn [_ exchange]
    (let [uri (.getRequestURI exchange)
          file (uri->file "." uri)]
      (if (.isDirectory file)
        (do (.add (.getResponseHeaders exchange)
                  "Content-Type" "text/html")
            (respond html-around
                     exchange
                     (html-links (str uri) (listing file))))
        (respond exchange (details file))))))


;; 위에 구현한 핸들러로 교체
(comment
  (def p (default-handler
          "There's no problem that can't be solved with another level of indirection"))

  (def server (new-server 8123 "/" p))

  (update-proxy p {"handle" fs-handler})
  (.stop server 0))