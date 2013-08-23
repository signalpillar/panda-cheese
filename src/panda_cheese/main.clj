(ns panda-cheese.main
  (:require [panda-cheese.core :as core]
            [docopt.core :as docopt])
  (:gen-class))


(def USAGE "Generate PNG for the provided TQL definition (XML)

Usage:
   panda_cheese [--show-cardinalities] <tql_src_path> <dest_file_name>
   panda_cheese -h | --help
   panda_cheese --version


Options:
   -h, --help            Show this screen
   --version             Show version
   --show-cardinalities  Show cardinality information
")


(defn #^{:doc USAGE
         :version "panda-cheese, version 0.1.4"}
  -main [ & args ]
  (binding [*ns* 'panda-cheese.main]
    (let [arg-map (docopt/docopt args)]
      (cond
       (or (nil? arg-map)
           (arg-map "--help"))                (println USAGE)
           (arg-map "--version")              (println (:version (meta #'-main)))
           (and (arg-map "<tql_src_path>")
                (arg-map "<dest_file_name>")) (core/generate-class-diagram
                                               (arg-map "<tql_src_path>")
                                               (arg-map "<dest_file_name>"))

            :otherwise                        (println USAGE)))))
