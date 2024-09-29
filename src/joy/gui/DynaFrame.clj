(ns joy.gui.DynaFrame
  (:gen-class
   :name joy.gui.DynaFrame
   :extends javax.swing.JFrame
   :implements [clojure.lang.IMeta]
   :state state
   :init init
   :constructors {[String] [String]
                  [] [String]}
   :prefix "df-"
   :methods [[display [java.awt.Container] void]
             ^{:static true} [version [] String]])
  (:import (javax.swing JFrame JPanel JComponent)
           (java.awt BorderLayout Container)))

;; failed: Extra input spec: :clojure.core.specs.alpha/ns-form
;; 위와 같은 종류의 오류가 나타나면 spec에 맞게 사용 되었는지 확인 한다.
;; 여기서는 prefix를 string으로 변경하니 해결 되었다.

;; gen-class를 사용하기 위해서는 
;; edn 파일에서 path에 class가 생성될 path를 추가 해준다.
;; path에 추가한 폴더를 생성 해준다.

;; 여기서는 gen-class와 :impl-ns가 동일 하다는 것을 전제 한다.
;; 분리하면 필요하지 않은 파일이 컴파일 되는 것을 예방 할 수 있다.

(defn df-init [title]
  [[title] (atom {::title title})])

(defn df-meta [this]
  @(.state this))

(defn version [] "1.0")

(defn df-display [this pane]
  (doto this
    (-> .getContentPane .removeAll)
    (.setContentPane (doto (JPanel.)
                       (.add pane BorderLayout/CENTER)))
    (.pack)
    (.setVisible true)))