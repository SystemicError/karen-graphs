(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'karen-graphs.core
   :output-to "out/karen_graphs.js"
   :output-dir "out"})
