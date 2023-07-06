(ns deercreeklabs.stockroom
  (:refer-clojure :exclude [get keys])
  (:require
   [deercreeklabs.stockroom.utils :as u]))

;;;;;;;;;;;;;;;;;;;; API ;;;;;;;;;;;;;;;;;;;;

(defn stockroom
  "Returns a new stockroom cache with the given capacity."
  [num-keys]
  (u/stockroom num-keys))

(defn get
  "Gets the value for the given key from the cache.
   Returns nil if the key is not present."
  [stockroom k]
  (u/get* stockroom k))

(defn keys
  "Returns all keys in the cache as a sequence."
  [stockroom]
  (u/keys* stockroom))

(defn put!
  "Adds the given key and value to the cache.
   Returns nil."
  [stockroom k v]
  (u/put! stockroom k v))

(defn evict!
  "Evicts the given key from the cache.
   Returns nil."
  [stockroom k]
  (u/evict! stockroom k))

(defn flush!
  "Flushes / empties the cache.
   Returns nil."
  [stockroom]
  (u/flush! stockroom))

(defn memoize-sr
  "Memoizes the given function, based on the arguments passed to the function.
   Optionally takes a `num-keys` argument which specifies the cache capacity.
   If `num-keys` is not provided, a cache of size 100 is used.
   Returns the memoized function."
  ([f]
   (memoize-sr f 100))
  ([f num-keys]
   (u/memoize-sr f num-keys)))
