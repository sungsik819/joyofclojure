(ns memoization)

;; 메모이제이션
;; 매번 호출 때마다 계산하지 않고, 재활용 가능
;; memoize에 의한 무분별한 저장이 항상 적합한 것은 아니다.
;; 메모이제이션 얀산을 추상화하여 일반화 하고, 도메인에 적용하는데 
;; 좀 더 적잘한 캐시 전략을 갖는 프레임워크를 구성하는 방법을 고민 하자

(def gcd (memoize
          (fn [x y]
            (cond
              (> x y) (recur (- x y) y)
              (< x y) (recur x (- y x))
              :else x))))

(gcd 1000645475 56130776629010010)

;; 메모이제이션 재실험하기

;; 캐싱을 위한 프로토콜
(defprotocol CacheProtocol
  (lookup [cache e])
  (has? [cache e])
  (hit [cache e])
  (miss [cache e ret]))

;; BasicCache 타입
(deftype BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (has? [_ item]
    (contains? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result))))

(def cache (BasicCache. {}))

(lookup (miss cache '(servo) :robot) '(servo))

(defn through [cache f item]
  (if (has? cache item)
    (hit cache item)
    (miss cache item (delay (apply f item)))))

;; 플러거블(pluggable)한 메모이제이션 타입 구현하기
(deftype PluggableMemoization [f cache]
  CacheProtocol
  (has? [_ item] (has? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (PluggableMemoization. f (miss cache item result)))
  (lookup [_ item]
    (lookup cache item)))

;; 추상화 지향 프로그래밍
;; 플러거블한 메모이제이션을 적용한 함수
(defn memoization-impl [cache-impl]
  (let [cache (atom cache-impl)]
    (with-meta
      (fn [& args]
        (let [cs (swap! cache through (.f cache-impl) args)]
          @(lookup cs args)))
      {:cache cache})))

;; 적용 연습
(def slowly (fn [x] (Thread/sleep 3000) x))
(def sometimes-slowy (memoization-impl
                      (PluggableMemoization.
                       slowly
                       (BasicCache. {}))))

(time [(sometimes-slowy 108) (sometimes-slowy 108)])

(time [(sometimes-slowy 108) (sometimes-slowy 108)])