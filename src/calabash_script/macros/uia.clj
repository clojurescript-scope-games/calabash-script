(ns calabash-script.macros.uia
  (:require [clojure.string :as st]))

(defmacro classes
  [& syms]
  (let [keywordize (fn [s]
            "transforms a symbol of form UIAXyzAsdFx to keyword :xyzAsdFx"
            (let [s (drop 3 (str s))
                  [fst & others] s]
              (keyword (apply str (cons (st/lower-case fst) others)))))
        pairs (map (fn [sym]
                     [(keywordize sym)
                      `(~'js* ~(str "((typeof " (name sym) " != 'undefined') && " (name sym) ")"))])
                   syms)]
    `(into {} [~@pairs])))


(defmacro ^{:private true} assert-args [fnname & pairs]
  `(do (when-not ~(first pairs)
         (throw (IllegalArgumentException.
                  ~(str fnname " requires " (second pairs)))))
     ~(let [more (nnext pairs)]
        (when more
          (list* `assert-args fnname more)))))

(defmacro query-let
  "bindings => binding-form test

  If test is true, evaluates then with binding-form bound to the value of
  test, if not, yields else"
  {:added "1.0"}
  ([bindings then]
     `(query-let ~bindings ~then nil))
  ([bindings then else & oldform]
     (assert-args
      (and (vector? bindings) (nil? oldform)) "a vector for its binding"
      (= 2 (count bindings)) "exactly 2 forms in binding vector")
     (let [form (bindings 0) query (bindings 1)]
       `(if-let [res# (seq (calabash-script.core/query-el ~query))]
          (let [~form res#]
            ~then)
          ~else))))
