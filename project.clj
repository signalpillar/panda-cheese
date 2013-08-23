(defproject panda-cheese "0.1.4"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :aot [panda-cheese.core]
  :main panda-cheese.main
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enlive "1.1.1"]
                 [docopt "0.6.1"]
                 [net.sourceforge.plantuml/plantuml "7971"]])
