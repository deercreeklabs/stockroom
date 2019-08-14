(ns deercreeklabs.stockroom.utils
  (:require
   [schema.core :as s]))

(defprotocol IStockroom
  (get* [this k])
  (keys* [this])
  (put! [this k v])
  (evict! [this k])
  (flush! [this]))

;;;;;;;;;;;;;;;;;;;; helper fns ;;;;;;;;;;;;;;;;;;;;

(defn put!* [num-keys k v state]
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

(defn init-state! [*state]
  (reset! *state {:key->index {}
                  :cache [] ;; [k v referenced?]
                  :clock-hand 0}))

(defrecord Stockroom [num-keys *state]
  IStockroom
  (get* [this k]
    (let [state (swap! *state (partial update-referenced? k))
          {:keys [key->index cache]} state]
      (when-let [index (key->index k)]
        (second (cache index)))))

  (keys* [this]
    (keys (:key->index @*state)))

  (put! [this k v]
    (swap! *state (partial put!* num-keys k v))
    nil)

  (evict! [this k]
    (swap! *state (fn [state]
                    (let [i (-> state :key->index (get k))]
                      (-> state
                          (update :key->index dissoc k)
                          (assoc-in [:cache i 2] false)
                          (assoc :clock-hand i)))))
    nil)

  (flush! [this]
    (init-state! *state)
    nil))

(defn stockroom [num-keys]
  (when-not (pos? num-keys)
    (throw (ex-info "num-keys must be positive."
                    {:type :illegal-argument
                     :subtype :num-keys-not-positive
                     :num-keys num-keys})))
  (let [*state (atom nil)]
    (init-state! *state)
    (->Stockroom num-keys *state)))

(defn memoize-sr
  [f num-keys]
  (let [sr (stockroom num-keys)]
    (fn [& args]
      (or (get sr args)
          (let [ret (apply f args)]
            (put! sr args ret)
            ret)))))
