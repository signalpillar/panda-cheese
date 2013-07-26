(ns panda-cheese.core
  (:use [clojure.xml :only [parse]]
        [clojure.pprint :only [pprint]]
        [clojure.string :only [join]]
        [clojure.java.io :only [file]])
  (:import [net.sourceforge.plantuml SourceStringReader])
  (:gen-class))


(defn find-tag [tag col]
  (first (filter #(= tag (:tag %)) col)))

(defn find-tag-in-content [src tag]
  (find-tag tag (:content src)))

(defn normalize-name [v]
  (.replaceAll v "\\s+" "_"))

(defn parse-where [src] ())

(defn parse-object [src]
  (let [name (get-in src [:attrs :name])
        class (get-in src [:attrs :class])
        where-src (find-tag-in-content src :where)]
    {:name name :class class
     :where (parse-where where-src)}))

(defn parse-link [src]
  (let [obj (parse-object src)
        to (get-in src [:attrs :to])
        from (get-in src [:attrs :from])]
    (assoc obj :link {:to to :from from})))


(defn find-all-links-recur [col result]
  (if-let [c (first col)]
    (if (= :tql:link-ref (:tag c))
      (find-all-links-recur (rest col) (conj result c))
      (find-all-links-recur (:content c)
                            (find-all-links-recur (rest col) result)))
    result))

(defn find-link-refs [elms]
  (find-all-links-recur elms []))

(defn parse-object-cardinalities [src]
  ; (println  (find-tag-in-content src :tql:where) )
  (let [links-src (-> src (find-tag-in-content :tql:where) (find-tag-in-content :tql:links))
        all-links (find-link-refs (:content links-src))
        attrs (map :attrs all-links)]
    (zipmap (map :name attrs) attrs)))

(defn parse-ci [src]
  (let [obj (parse-object src)
        cardinalities (parse-object-cardinalities src)]
    (assoc obj :cardinalities cardinalities)))

(defmulti parse-tql-node :tag)
(defmethod parse-tql-node :tql:node
  [src] (parse-ci src))
(defmethod parse-tql-node :tql:link
  [src] (parse-link src))
(defmethod parse-tql-node :tql:function-link
  [src] (parse-link src))
(defmethod parse-tql-node :tql:where
  [src] ())
(defmethod parse-tql-node :default
  [src] nil)

(defn parse-tql [src]
  (let [resource-src (find-tag-in-content src :resource)
        tql-name (get-in resource-src [:attrs :name])
        nodes (keep parse-tql-node (:content resource-src))]
    [tql-name nodes]))

(def PLANTUML-TEMPLATE-CLASS-MODEL
  "@startuml %s.png
skinparam monochrome true
hide circle
' hide member
hide empty members
hide empty methods
%s

%s
@enduml")

(defn draw-cardinalities [min max]
  (cond
   (and (not min) (not max)) "1..*"
   (and min (not max)) (format "%s..*" min)
   (and (not min) max) (format "1..%s" max)
   (and min max) (format "%s..%s" min max)))

(defn get-cardinality [ci-name ci link-name]
  (let [cardinalities (get-in ci [:cardinalities link-name])
        [min max] ((juxt :min-occurs :max-occurs) cardinalities)]
    (draw-cardinalities min max)))

(defn to-plantuml-link [link arrow-str object-by-name]
  (let [link-attrs (:link link)
        class (:class link)
        name (:name link)
        from (:from link-attrs)
        from-ci (object-by-name (normalize-name from))
        from-cardinality (get-cardinality from from-ci name)
        to (:to link-attrs)
        to-ci (object-by-name (normalize-name to))
        to-cardinality (get-cardinality to to-ci name)]
    (format "%s \"%s\" %s \"%s\" %s : %s"
            (normalize-name from) from-cardinality
            arrow-str
            to-cardinality (normalize-name to)
            class)))

(defmulti draw-link :class)
(defmethod draw-link "join_f"
  [link object-by-name] (to-plantuml-link link "..>" object-by-name))
(defmethod draw-link :default
  [link object-by-name] (to-plantuml-link link "-->" object-by-name))

(defn to-plantuml-object [object]
  (let [name (:name object)
        cit (:class object)]
    (format "class \"%s\" as %s << %s >> " name (normalize-name name) cit)))

(defn mapcat-lines [fn col]
  (join "\n" (doall (map fn col))))

(defn to-plantuml [name nodes]
  (let [node-by-name (zipmap (map :name nodes) nodes)
        [links objects] ((juxt filter remove) :link nodes)
        object-normalized-names (map (comp normalize-name :name) objects)
        object-by-name (zipmap object-normalized-names objects)
        link-uml (mapcat-lines #(draw-link % object-by-name) links)
        obj-uml (mapcat-lines to-plantuml-object objects)]
    (format PLANTUML-TEMPLATE-CLASS-MODEL name obj-uml link-uml )))

(defn store [path diagram-text]
  (doto (SourceStringReader. diagram-text)
    (.generateImage (file path))))

(defn generate-class-diagram
  "Generate class diagram for parsed TQL source and store diagram as .png
   according to specified file path"
  ([tql-source file-path]
     (let [[tql-name nodes] (parse-tql tql-source)
           diagram (to-plantuml tql-name nodes)]
       (println diagram)
       (store file-path diagram)))
  "Generate class diagram for the specified TQL file and store image with
  the same name but with png extension"
  ([tql-file]
     (let [tql-source (parse tql-file)
           tql-name (.getName tql-file)
           file-path (format "%s.png" tql-name)]
       (generate-class-diagram tql-source file-path))))
