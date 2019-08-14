# stockroom

A in-memory, fixed-size cache for Clojure / Clojurescript.
Keys and values are arbitrary Clojure data.
Uses the Clock caching algorithm:
https://en.wikipedia.org/wiki/Page_replacement_algorithm#Clock

## Installation via Leiningen / Clojars:

[![Clojars Project](http://clojars.org/deercreeklabs/stockroom/latest-version.svg)](http://clojars.org/deercreeklabs/stockroom)

# API Documentation
All public functions are in the `deercreeklabs.stockroom`
namespace. All other namespaces should be considered private implementation
details that may change.

-------------------------------------------------------------------------------
### stockroom
```clojure
(stockroom num-keys)
```
Creates a stockroom cache with the given capacity.

#### Parameters
* `num-keys`: The number of keys this cache will contain.

#### Return Value
The created stockroom cache.

#### Example
```clojure
(stockroom 100)
```

-------------------------------------------------------------------------------
### get
```clojure
(get stockroom k)
```
Gets the value for the given key from the cache.

#### Parameters
* `stockroom`: The stockroom cache
* `k`: The key to retrieve

#### Return Value
The value of the given key, or nil if the key is not present

#### Example
```clojure
(get stockroom "a")
```

-------------------------------------------------------------------------------
### keys
```clojure
(keys stockroom)
```
Returns all keys in the cache as a sequence.

#### Parameters
* `stockroom`: The stockroom cache

#### Return Value
All keys in the cache as a sequence.

#### Example
```clojure
(keys stockroom)
```

-------------------------------------------------------------------------------
### put!
```clojure
(put! stockroom k v)
```
Puts the given value for the given key into the cache.

#### Parameters
* `stockroom`: The stockroom cache
* `k`: The key
* `v`: The value

#### Return Value
`nil`

#### Example
```clojure
(put! stockroom :a-key "a-value")
```

-------------------------------------------------------------------------------
### evict!
```clojure
(evict! stockroom k)
```
Evicts the given key from the cache.

#### Parameters
* `stockroom`: The stockroom cache
* `k`: The key

#### Return Value
`nil`

#### Example
```clojure
(evict! stockroom "a key you don't want")
```

-------------------------------------------------------------------------------
### flush!
```clojure
(flush! stockroom)
```
Flushes / empties the cache.

#### Parameters
* `stockroom`: The stockroom cache

#### Return Value
`nil`

#### Example
```clojure
(flush! stockroom)
```

-------------------------------------------------------------------------------
### memoize-sr
```clojure
(memoize-sr stockroom f)
(memoize-sr stockroom f num-keys)
```
Memoizes the given function, based on the arguments passed to the function.
Optionally takes a `num-keys` argument which specifies the cache capacity.
If `num-keys` is not provided, a cache of size 100 is used.

#### Parameters
* `stockroom`: The stockroom cache
* `num-keys`: The number of keys this cache will contain.

#### Return Value
The memoized function.

#### Example
```clojure
(memoize-sr stockroom an-expensive-fn 500)
```


-------------------------------------------------------------------------------

## License

Original work (c) 2016 FarBetter, Inc.
Modified work (c) 2017 Deer Creek Labs, LLC

Distributed under the Apache Software License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0.txt
