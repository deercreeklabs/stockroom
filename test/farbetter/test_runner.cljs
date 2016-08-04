(ns farbetter.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :as test :refer-macros [run-tests]]
   [farbetter.stockroom-test]))

(nodejs/enable-util-print!)

(defn -main [& args]
  (run-tests 'farbetter.stockroom-test))

(set! *main-cli-fn* -main)
