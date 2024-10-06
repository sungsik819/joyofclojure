(ns joy.patterns.abstract-factory)

(def config
  `{:systems {:pump {:type :feeder :descr "Feeder system"}
              :sim1 {:type :sim :fidelity :low}
              :sim2 {:type :sim :fidelity :high :threads 2}}})

(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])

(describe-system :pump {:type :feeder :descr "Feeder system"})

;; 추상 팩토리 멀티 메서드 구성
(defmulti construct describe-system)

(defmethod construct :default [name cfg]
  {:name name
   :type (:type cfg)})

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(comment
  (construct-subsystems (:systems config))
  ;; ({:name :pump, :type :feeder} {:name :sim2, :type :sim} {:name :sim1, :type :sim})
  )

(defmethod construct [:feeder nil]
  [_ cfg]
  (:descr cfg))

(comment
  (construct-subsystems (:systems config))
  ;; ("Feeder system" {:name :sim2, :type :sim} {:name :sim1, :type :sim})
  )



(defrecord LowFiSim [name])
(defrecord HiFiSim [name threads])

;; 멀티메서드를 사용하여 구상(concrete) 팩토리 정의하기
(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name))

(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg)))


;; 함수를 그대로 사용
(comment
  (construct-subsystems (:systems config))
  ;; ("Feeder system" {:name :sim2, :threads 2} {:name :sim1})
  )