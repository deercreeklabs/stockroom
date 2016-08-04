(ns farbetter.stockroom
  (:refer-clojure :exclude [get keys])
  (:require
   [farbetter.utils :as u :refer
    [throw-far-error #?@(:clj [inspect sym-map])]]
   [schema.core :as s :include-macros true]
   [taoensso.timbre :as timbre
    #?(:clj :refer :cljs :refer-macros) [debugf errorf infof]])
  #?(:cljs
     (:require-macros
      [farbetter.utils :refer [inspect sym-map]])))

(declare get-cache-item put* update-referenced?)

(def Key s/Any)
(def Value s/Any)
(def Index s/Int)
(def CacheItem [(s/one Key :key)
                (s/one Value :value)
                (s/one s/Bool :referenced?)])
(def State {:key->index {Key Index}
            :cache [CacheItem]
            :clock-hand Index})

(defprotocol ICache
  (get- [this k])
  (put- [this k v])
  (keys- [this]))

(defrecord Stockroom [num-keys state-atom]
  ICache
  (get- [this k]
    (let [state (swap! state-atom (partial update-referenced? k))
          {:keys [key->index cache]} state]
      (when-let [index (key->index k)]
        (second (cache index)))))

  (put- [this k v]
    (swap! state-atom (partial put* num-keys k v))
    nil)

  (keys- [this]
    (clojure.core/keys (:key->index @state-atom))))

;;;;;;;;;;;;;;;;;;;; API ;;;;;;;;;;;;;;;;;;;;

(s/defn make-stockroom :- Stockroom
  [num-keys :- s/Int]
  (let [state {:key->index {}
               :cache []
               :clock-hand 0}
        state-atom (atom state)]
    ;; These are left here for debugging
    ;; (set-validator! state-atom #(s/validate State %))
    ;; (add-watch state-atom :print (fn [k r old new]
    ;;                                (debugf "###### State Change #######\n%s"
    ;;                                        (u/inspect-str old new))))
    (map->Stockroom (sym-map num-keys state-atom))))

(s/defn get :- (s/maybe Value)
  [stockroom :- Stockroom
   k :- Key]
  (get- stockroom k))

(s/defn put :- nil
  [stockroom :- Stockroom
   k :- Key
   v :- Value]
  (put- stockroom k v))

(s/defn keys :- [Key]
  [stockroom :- Stockroom]
  (keys- stockroom))

;;;;;;;;;;;;;;;;;;;; Helper fns ;;;;;;;;;;;;;;;;;;;;

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
