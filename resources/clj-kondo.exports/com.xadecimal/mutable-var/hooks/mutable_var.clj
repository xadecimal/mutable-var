(ns hooks.mutable-var
  (:require [clj-kondo.hooks-api :as api]))

(defn var-scope*
  [children]
  (letfn [(transform [children]
            (loop [result []
                   remaining children]
              (if (empty? remaining)
                result
                (let [[node & tail] remaining]
                  (cond
                    (and (api/list-node? node)
                         (= 3 (count (:children node)))
                         (= (api/sexpr (first (:children node))) 'var))
                    (let [name (second (:children node))
                          value (last (:children node))]
                      (recur (conj result (with-meta
                                            (api/list-node
                                             (list*
                                              (api/coerce `let)
                                              (api/vector-node [name value])
                                              (transform tail)))
                                            (meta node)))
                             []))
                    (and (api/list-node? node)
                         (= (api/sexpr (first (:children node))) 'var-scope))
                    (recur (conj result (var-scope* (:children node))) tail)
                    (api/list-node? node)
                    (recur (conj result (with-meta
                                          (api/list-node (transform (:children node)))
                                          (meta node))) tail)
                    (and (api/token-node? node)
                         (= (api/sexpr node) 'var-scope))
                    (recur result tail)
                    :else
                    (recur (conj result node) tail))))))]
    (api/list-node (cons (api/coerce 'do) (transform children)))))

(defn var-scope [{:keys [node]}]
  (let [children (:children node)
        new-node (var-scope* children)]
    {:node new-node}))

#_(hooks.mutable-var/var-scope {:node (api/parse-string "(var-scope
            (var i 0)
            (when (= i 0)
              (var-scope
               (var i 10)
               (is (= i 10))))
            i)")})

#_(do (require '[clj-kondo.core :as clj-kondo])
      (def code "(require '[com.xadecimal.mutable-var :as m]) (m/var-scope (var i 0) (set! i (inc i)) i)")
      (binding [api/*reload* true]
        (:findings (with-in-str code (clj-kondo/run! {:lint ["-"]})))))
