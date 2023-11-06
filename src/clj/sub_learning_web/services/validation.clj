(ns sub-learning-web.services.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))


(defn same-as [k]
  (fn [value subject]
    (= value (k subject))))


(defn validate-signup? [form]
  (let [errors (first (b/validate form
        :email v/required
        :confirmation v/required
        :password [v/required [v/min-count 6]]))]
    (if (not (= (:password form) (:confirmation form)))
      (merge errors {:password "entered passwords must match"})
      errors)))

(defn validate-contact? [form]
  (let [errors (first (b/validate form
                        :email v/required
                        :subject v/required
                        :body v/required))]
    errors))
