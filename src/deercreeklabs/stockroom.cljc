(ns deercreeklabs.stockroom
  (:refer-clojure :exclude [get keys])
  (:require
   [deercreeklabs.stockroom.utils :as u]
   [schema.core :as s]))

(def Key s/Any)
(def Value s/Any)
(def Function s/Any)
(def Stockroom (s/protocol u/IStockroom))

;;;;;;;;;;;;;;;;;;;; API ;;;;;;;;;;;;;;;;;;;;

(s/defn stockroom :- Stockroom
  "Returns a new stockroom cache with the given capacity."
  [num-keys :- s/Int]
  (u/stockroom num-keys))

(s/defn get :- (s/maybe Value)
  "Gets the value for the given key from the cache.
   Returns nil if the key is not present."
  [stockroom :- Stockroom
   k :- Key]
  (u/get* stockroom k))

(s/defn keys :- [Key]
  "Returns all keys in the cache as a sequence."
  [stockroom :- Stockroom]
  (u/keys* stockroom))

(s/defn put! :- (s/eq nil)
  "Adds the given key and value to the cache.
   Returns nil."
  [stockroom :- Stockroom
   k :- Key
   v :- Value]
  (u/put! stockroom k v))

(s/defn evict! :- (s/eq nil)
  "Evicts the given key from the cache.
   Returns nil."
  [stockroom :- Stockroom
   k :- Key]
  (u/evict! stockroom k))

(s/defn flush! :- (s/eq nil)
  "Flushes / empties the cache.
   Returns nil."
  [stockroom :- Stockroom]
  (u/flush! stockroom))

(s/defn memoize-sr :- Function
  "Memoizes the given function, based on the arguments passed to the function.
   Optionally takes a `num-keys` argument which specifies the cache capacity.
   If `num-keys` is not provided, a cache of size 100 is used.
   Returns the memoized function."
  ([f :- Function]
   (memoize-sr f 100))
  ([f :- Function
    num-keys :- s/Int]
   (u/memoize-sr f num-keys)))
