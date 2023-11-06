(ns sub-learning-web.services.review
  (:require [sub-learning-web.layout :as layout]
            [ring.util.http-response :as response]
            [sub-learning-web.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-status :as status]
            [sub-learning-web.services.validation :as v]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [sub-learning-web.services.quiz :as quiz]
            [sub-learning-web.env :as env]))

(defn review [request]
  "Main page to handle review quizes"
  (if-not (authenticated? (:session request))
    (throw-unauthorized)
    (let [userid (get-in request [:session :identity])
          save-param (get-in request [:params :save])
          done-param (get-in request [:params :done])]
      (if-not (nil? save-param)
        (db/update-review! (quiz/review-string-to-map save-param)))
      (if-not (nil? done-param)
        (db/delete-review! (quiz/review-string-to-map done-param)))
      (let [review-item (db/get-first-review {:userid userid})]
        (if-not (nil? review-item)
          (let [query-elements (quiz/get-quiz-elements review-item)
                save-string (quiz/create-save-string review-item)]
            (layout/render
                "review.html"
                (merge query-elements
                       {:current-review-str save-string}
                       {:session (:session request)}
                       {:hosturl env/host-url})))
          (layout/render "no-reviews.html" {:session (:session request)}))))))
