(ns loom.test.gen
  (:require [clojure.test :refer (deftest testing is are)]
            [loom.graph :refer (graph digraph weighted-graph weighted-digraph graph? nodes edges)]
            [loom.gen :refer (gen-circle gen-newman-watts)]))

(deftest build-circle-test
  (let [g1 (gen-circle (graph) 5 1)
        g2 (gen-circle (graph) 6 2)
        g3 (gen-circle (digraph) 6 2)
        g4 (gen-circle (weighted-graph {0 {1 42 2 42} 1 {2 42 3 42} 2 {3 42 4 42}}) 10 1)
        g5 (gen-circle (weighted-graph) 10 1)
        g6 (gen-circle (weighted-digraph [0 1 42] [1 2 42] [2 3 42] [1 0 43] [2 1 43] [3 2 43]) 10 1)
        g7 (gen-circle (weighted-digraph) 10 1)
        ]
    (testing "Generating circle-graphs from the different graph types"
      (are [graphs] (graph? graphs)
        g1
        g2
        g3
        g4
        g5
        g6
        g7))
    (testing "Nodes and Edges"
      (are [expected got] (= expected got)
        (into #{} (range 5)) (nodes g1)
        (into #{} (range 6)) (nodes g2)
        (into #{} (range 6)) (nodes g3)
        (into #{} (range 10)) (nodes g4)
        (into #{} (range 10)) (nodes g5)
        (into #{} (range 10)) (nodes g6)
        (into #{} (range 10)) (nodes g7)
        #{[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3] [4 0] [0 4]} (set (edges g1))
        #{[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3] [4 5] [5 4] [5 0] [0 5]
          [0 2] [2 0] [1 3] [3 1] [2 4] [4 2] [3 5] [5 3] [4 0] [0 4] [1 5] [5 1]} (set (edges g2))
        #{[0 1] [1 2] [2 3] [3 4] [4 5] [5 0] [0 2] [1 3] [2 4] [3 5] [4 0] [5 1]} (set (edges g3))
        #{[0 1] [1 2] [2 3] [3 4] [4 5] [5 6] [6 7] [7 8] [8 9] [9 0]
          [0 2] [1 3] [2 4]
          [1 0] [2 1] [3 2] [4 3] [5 4] [6 5] [7 6] [8 7] [9 8] [0 9]
          [2 0] [3 1] [4 2]} (set (edges g4))
        #{[0 1] [1 2] [2 3] [3 4] [4 5] [5 6] [6 7] [7 8] [8 9] [9 0]
          [1 0] [2 1] [3 2] [4 3] [5 4] [6 5] [7 6] [8 7] [9 8] [0 9]} (set (edges g5))
        #{[0 1] [1 2] [2 3] [3 4] [4 5] [5 6] [6 7] [7 8] [8 9] [9 0]
          [1 0] [2 1] [3 2]} (set (edges g6))
        #{[0 1] [1 2] [2 3] [3 4] [4 5] [5 6] [6 7] [7 8] [8 9] [9 0]} (set (edges g7))))))