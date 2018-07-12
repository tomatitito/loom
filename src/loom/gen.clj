(ns ^{:doc "Graph-generating functions"
      :author "Justin Kramer"}
  loom.gen
  (:require [loom.graph :refer [weighted? directed? add-nodes* add-edges* nodes]]))

(defn gen-rand
  "Adds num-nodes nodes and approximately num-edges edges to graph g. Nodes
  used for each edge are chosen at random and may be chosen more than once."
  [g num-nodes num-edges & {:keys [min-weight max-weight loops seed]
                            :or {min-weight 1
                                 max-weight 1
                                 loops false
                                 seed (System/nanoTime)}}]
  {:pre [(or (not (weighted? g)) (< min-weight max-weight))]}
  (let [rnd (java.util.Random. seed)
        rand-w #(+ (.nextInt rnd (- max-weight min-weight)) min-weight)
        rand-n #(.nextInt rnd num-nodes)
        weighted? (weighted? g)
        nodes (range num-nodes)
        edges (for [_ (range num-edges)
                    :let [n1 (rand-n) n2 (rand-n)]
                    :when (or loops (not= n1 n2))]
                (if weighted?
                  [n1 n2 (rand-w)]
                  [n1 n2]))]
    (-> g
        (add-nodes* nodes)
        (add-edges* edges))))

(defn gen-rand-p
  "Adds num-nodes nodes to graph g with the probably p of an edge between
  each node."
  [g num-nodes p & {:keys [min-weight max-weight loops seed]
                    :or {min-weight 1
                         max-weight 1
                         loops false
                         seed (System/nanoTime)}}]
  {:pre [(or (not (weighted? g)) (< min-weight max-weight))]}
  (let [rnd (java.util.Random. seed)
        rand-w #(+ (.nextInt rnd (- max-weight min-weight)) min-weight)
        directed? (directed? g)
        weighted? (weighted? g)
        nodes (range num-nodes)
        edges (for [n1 nodes n2 nodes
                    :when (and (if directed?
                                 (or loops (not= n1 n2))
                                 (or (> n1 n2)
                                     (and loops (= n1 n2))))
                               (> p (.nextDouble rnd)))]
                (if weighted?
                  [n1 n2 (rand-w)]
                  [n1 n2]))]
    (-> g
        (add-nodes* nodes)
        (add-edges* edges))))

(defn gen-circle
  "Adds num-nodes nodes to graph g and connects each one to out-degree
  other nodes."
  [g num-nodes out-degree]
  {:pre [(> num-nodes (* out-degree 2))]}
  (let [nodes (range num-nodes)
        edges (for [n nodes
                    d (range 1 (inc out-degree))]
                [n (mod (+ n d) (count nodes))])]
    (-> g
        (add-nodes* nodes)
        (add-edges* edges))))

(defn ^:private add-shortcuts
  "Computes additional edges for graph g as described in Newman and Watts (1999)."
  ([g phi seed]
   (let [rnd (java.util.Random. seed)
         nodes (loom.graph/nodes g)
         shortcuts (for [n nodes
                         :when (> phi (.nextDouble rnd))]
                     [n (.nextInt rnd (count nodes))])]
     (-> g
         (add-edges* shortcuts)))))

(defn gen-newman-watts
  "Generate a graph with small-world properties as described in Newman and Watts
  (1999)."
  ([g num-nodes out-degree phi seed]
   (-> g
       (gen-circle num-nodes out-degree)
       (add-shortcuts phi seed)))
  ([g num-nodes out-degree phi]
    (gen-newman-watts g num-nodes out-degree phi (System/nanoTime))))


(defn ^:private initial-barabasi-albert
  "In the Barabasi-Albert model, the first new node that is
  added to the graph has to be treated specially. It will have
  exactly num-edges connections, and every node in the graph
  will attach to it with equal probability."
  [g num-initial num-edges rnd]
  ;; there can't be more edges that nodes in the graph
  {:pre [(<= num-edges num-initial)]}
  (let [num-nodes (count (nodes g))
        new-node (inc num-nodes)
        draw-connecting-nodes (fn [g]
                                (loop [partners #{}]
                                  (if (= (count partners) num-edges)
                                    (vec partners)
                                    (recur (conj partners (.nextInt rnd num-initial))))))
        new-edges (map vector (repeat num-edges new-node) (draw-connecting-nodes g))]
    (add-edges* g new-edges)))


;; collect all degrees in a list
;(map #(count (loom.graph/successors g0 %)) (nodes g0))
;; sum over all degrees
;(reduce #(+ %1 (count (loom.graph/successors g0 %2))) 0 (nodes g0))

;(defn connect?
;  "Should old be connected to a new node?"
;  [g old degree-sum rnd]
;  (let [degree-old (loom.graph/out-degree g old)
;        p (/ degree-old degree-sum)]
;    (<= p (.nextDouble rnd))))
(defn connect-to?
  "Higher-order function to decide wether node should be connected to.
  pred-fn must return true or false."
  [g node pred-fn]
  (pred-fn g node))


(defn gen-barabasi-albert
  "Generate a preferential attachment graph as described in Barabasi
  and Albert (1999)."
  [g num-initial num-nodes num-edges seed]
  (let [rnd (java.util.Random. seed)
        g-0 (initial-barabasi-albert g num-initial num-edges seed)
        connect-pred (fn [g node degree-sum rnd]
                       (let [d-node (count (loom.graph/successors g node))
                             degree-sum (reduce #(+ %1 (count (loom.graph/successors g-0 %2))) 0 (nodes g-0))]
                         (<= (/ d-node degree-sum) (.nextDouble rnd))))]
    g-0)
  )

