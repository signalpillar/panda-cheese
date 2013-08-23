(ns panda-cheese.main
  (:require [panda-cheese.core :as core]
            [docopt.core :as docopt])
  (:gen-class))

(def USAGE "panda-cheese

Usage:
   panda_cheese <tql_src_path> <dest_file_name>")


;; Add such options
;; Options:
;;    -h, --help           Show this screen
;;    --version            Show version
;;    --show-cardinalities Show cardinality information

(defn #^{:doc "documentation"
         :version "panda-cheese, version 0.1.4"}
  -main
  [ & args ]
  (if-not (= 2 (count args))
    (println USAGE)
    (let [[src dst] args]
      (core/generate-class-diagram src dst)
      (comment
        (let [arg-map (docopt/docopt args)]
          (cond
           (arg-map "--help")                 (println (:doc     (meta #'-main)))
           (arg-map "--version")              (println (:version (meta #'-main)))
           (and (arg-map "<tql_src_path>") (arg-map "<dest_file_name>"))
           (core/generate-class-diagram
            (arg-map "<tql_src_path>")
            (arg-map "<dest_file_name>"))

           :otherwise                         (println (:doc     (meta #'-main)))))))))
