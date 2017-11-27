(ns deercreeklabs.doo-test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [deercreeklabs.stockroom-test]))

(enable-console-print!)

(doo-tests 'deercreeklabs.stockroom-test)
