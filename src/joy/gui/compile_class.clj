(ns joy.gui.compile-class)

(compile 'joy.gui.DynaFrame)

(meta (joy.gui.DynaFrame. "3rd"))

(joy.gui.DynaFrame/version)

(def gui (joy.gui.DynaFrame. "4th"))

;; gui 실행
(.display gui (doto (javax.swing.JPanel.)
                (.add (javax.swing.JLabel. "Charlemagne and Pippin"))))

;; 내용 변경
(.display gui (doto (javax.swing.JPanel.)
                (.add (javax.swing.JLabel. "Mater semper certa est."))))
