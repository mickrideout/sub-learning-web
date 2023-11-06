(ns sub-learning-web.test.services.quiz
  (:require [sub-learning-web.services.quiz :as quiz]
            [clojure.test :refer :all]))

(deftest parse-int-test
  (are [in out] (= out (quiz/parse-int in))
       "5" 5
       "101" 101
       "-1" -1))

(deftest update-vals-map-test
  (let [string-map1 {:id "123" :anotherid "999" :blah "blah"}
              string-map2 {:hi "11" :there "777" :what "what"}]
    (is (= {:id 123 :anotherid 999 :blah "blah"} (quiz/update-vals string-map1 [:id :anotherid] quiz/parse-int)))
    (is (= {:hi 11 :there 777 :what "what"} (quiz/update-vals string-map2 [:hi :there] quiz/parse-int)))))

(deftest conditional-str-concat-test
  (are [in1 in2 result] (= result (quiz/conditional-str-concat in1 in2))
       "hi" "there" "hithere"
       "oh" "no" "ohno"
       "oh" "oh" "oh"
       "oh" nil "oh"
       "hey" "" "hey"))

(deftest find-first-test
  (are [col result] (is (= result (quiz/find-first #(not (nil? %)) col)))
       ["hi" nil "there"] "hi"
       [nil nil "there"] "there"
       ["boo"] "boo"
       [] nil
       nil nil))

