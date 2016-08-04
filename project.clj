(defproject farbetter/stockroom "0.1.2-SNAPSHOT"
  :description "Clojure/Clojurescript cache, using the Clock algorithm."
  :url "http://www.farbetter.com"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :lein-release {:scm :git
                 :deploy-via :clojars}
  :deploy-repositories [["clojars" {:creds :gpg}]]

  :profiles {:dev
             {:plugins
              [[lein-cljsbuild "1.1.3"]]}}

  :dependencies
  [[cljsjs/nodejs-externs "1.0.4-1"]
   [com.taoensso/timbre "4.7.3"]
   [farbetter/utils "0.1.51"]
   [org.clojure/clojure "1.8.0"]
   [org.clojure/clojurescript "1.9.93"]
   [prismatic/schema "1.1.3"]]

  :cljsbuild
  {:builds
   [{:id "node-test-none"
     :source-paths ["src" "test"]
     :notify-command ["node" "target/test/node_test_none/test_main.js"]
     :compiler
     {:optimizations :none
      :main "farbetter.test-runner"
      :target :nodejs
      :output-to "target/test/node_test_none/test_main.js"
      :output-dir "target/test/node_test_none"
      :source-map true}}
    {:id "node-test-adv"
     :source-paths ["src" "test"]
     :notify-command ["node" "target/test/node_test_adv/test_main.js"]
     :compiler
     {:optimizations :advanced
      :main "farbetter.test-runner"
      :target :nodejs
      :static-fns true
      :output-to  "target/test/node_test_adv/test_main.js"
      :output-dir "target/test/node_test_adv"
      :source-map "target/test/node_test_adv/map.js.map"}}]}

  :aliases
  {"auto-test-cljs" ["do"
                     "clean,"
                     "cljsbuild" "auto" "node-test-none"]
   "auto-test-cljs-adv" ["do"
                         "clean,"
                         "cljsbuild" "auto" "node-test-adv"]})
