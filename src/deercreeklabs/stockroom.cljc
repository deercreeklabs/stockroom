(ns deercreeklabs.stockroom
  (:refer-clojure :exclude [get keys])
  (:require
   [schema.core :as s :include-macros true]))

(declare get-cache-item put* shrink-cache update-referenced?)

(def Key s/Any)
(def Value s/Any)
(def Index s/Int)
(def Function s/Any)
(def CacheItem [(s/one Key :key)
                (s/one Value :value)
                (s/one s/Bool :referenced?)])
(def State {:key->index {Key Index}
            :cache [CacheItem]
            :clock-hand Index})

(defprotocol ICache
  (get- [this k])
  (put- [this k v])
  (keys- [this])
  (set-num-keys!- [this num-keys])
  (get-cur-num-keys- [this]))

(defrecord Stockroom [num-keys-atom state-atom]
  ICache
  (get- [this k]
    (let [state (swap! state-atom (partial update-referenced? k))
          {:keys [key->index cache]} state]
      (when-let [index (key->index k)]
        (second (cache index)))))

  (put- [this k v]
    (swap! state-atom (partial put* @num-keys-atom k v))
    nil)

  (keys- [this]
    (clojure.core/keys (:key->index @state-atom)))

  (set-num-keys!- [this num-keys]
    (when-not (pos? num-keys)
      (throw (ex-info "num-keys must be positive."
                      {:type :illegal-argument
                       :subtype :num-keys-not-positive
                       :num-keys num-keys})))
    (when (< num-keys @num-keys-atom)
      (swap! state-atom shrink-cache num-keys))
    (reset! num-keys-atom num-keys)
    nil)

  (get-cur-num-keys- [this]
    (count (:cache @state-atom))))

;;;;;;;;;;;;;;;;;;;; API ;;;;;;;;;;;;;;;;;;;;

(s/defn stockroom :- Stockroom
  [num-keys :- s/Int]
  (let [state {:key->index {}
               :cache []
               :clock-hand 0}
        state-atom (atom state)
        num-keys-atom (atom num-keys)]
    (->Stockroom num-keys-atom state-atom)))

(s/defn get :- (s/maybe Value)
  [stockroom :- Stockroom
   k :- Key]
  (get- stockroom k))

(s/defn put :- (s/eq nil)
  [stockroom :- Stockroom
   k :- Key
   v :- Value]
  (put- stockroom k v)
  nil)

(s/defn keys :- [Key]
  [stockroom :- Stockroom]
  (keys- stockroom))

(s/defn set-num-keys! :- (s/eq nil)
  [stockroom :- Stockroom
   num-keys :- s/Int]
  (set-num-keys!- stockroom num-keys))

(s/defn get-cur-num-keys :- s/Int
  [stockroom :- Stockroom]
  (get-cur-num-keys- stockroom))

(s/defn memoize-sr :- Function
  ([f :- Function]
   (memoize-sr f 100))
  ([f :- Function
    num-keys :- s/Int]
   (let [sr (stockroom num-keys)]
     (fn [& args]
       (or (get sr args)
           (let [ret (apply f args)]
             (put sr args ret)
             ret))))))

;;;;;;;;;;;;;;;;;;;; helper fns ;;;;;;;;;;;;;;;;;;;;

(defn put* [num-keys k v state]
  (loop [state state]
    (let [{:keys [clock-hand cache]} state]
      (cond
        ;; Cache vector is not yet full, so add another slot
        (< (count cache) num-keys)
        (-> state
            (update :cache conj [k v true])
            (assoc-in [:key->index k] (count cache))
            (assoc :clock-hand (count cache)))

        ;; Handle wrap around
        (>= clock-hand num-keys)
        (recur (assoc state :clock-hand 0))

        :else
        (let [[old-k old-v referenced?] (cache clock-hand)]
          (if-not referenced?
            (-> state
                (assoc-in [:cache clock-hand] [k v true])
                (update :key->index dissoc old-k)
                (update :key->index assoc k clock-hand)
                (update :clock-hand inc))
            (recur (-> state
                       (assoc-in [:cache clock-hand 2] false)
                       (update :clock-hand inc)))))))))

(defn update-referenced? [k state]
  (let [{:keys [key->index]} state]
    (if-let [index (key->index k)]
      (assoc-in state [:cache index 2] true)
      state)))

(defn shrink-cache [old-state num-keys]
  (let [{:keys [cache key->index clock-hand]} old-state
        extra-items (subvec cache num-keys)
        cache (subvec cache 0 num-keys)
        key->index (reduce (fn [acc [k v ref?]]
                             (dissoc acc k))
                           key->index extra-items)
        clock-hand (if (>= clock-hand num-keys)
                     0
                     clock-hand)]
    {:cache cache
     :key->index key->index
     :clock-hand clock-hand}))
