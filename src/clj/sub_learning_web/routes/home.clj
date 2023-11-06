(ns sub-learning-web.routes.home
  (:require [sub-learning-web.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [sub-learning-web.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-status :as status]
            [clojure.string :as str]
            [sub-learning-web.services.admin :as admin]
            [sub-learning-web.services.quiz :as quiz]
            [sub-learning-web.services.review :as review]
            [sub-learning-web.services.search :as search]))

(defn home-page [request]
  (let [cookie-next-page (:value (get (:cookies request) "next-page"))
        clear  (:clear (:params request))]
    (if (and (not (nil? cookie-next-page)) (empty? clear))
      (response/found cookie-next-page)
      (layout/render
        "home.html"
         {:user-message (:user-message (:flash request))
          :session (:session request)}))))

(defn about-page [request]
  (layout/render "about.html"
    {:session (:session request)}))

(defn pick-subtitle [request]
  "Show the pick subtitle page"
  (layout/render
    "pick.html" (select-keys (:params request) [:fromlang :tolang])))

(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/about" request (about-page request))
  (GET "/sub/:id" request (quiz/sub-page request))
  (GET "/sub/:id/:lineid" request (quiz/sub-page request))
  (GET "/sub/:id/:lineid/:rev" request (quiz/sub-page request))
  (GET "/quiz/:fromlang/:tolang" [fromlang tolang] (quiz/random-subtitle fromlang tolang))
  (GET "/returntopick/:id/:rev" request (quiz/redirect-to-pick request))
  (GET "/pairs/:language" request (quiz/get-language-pairs request))
  (GET "/login" request (admin/login-page request))
  (POST "/login" request (admin/login-authenticate request))
  (GET "/signup" request (admin/signup-page request))
  (POST "/signup" request (admin/signup-page-submit request))
  (GET "/contact" request (admin/contact request))
  (POST "/contact" request (admin/contact-submission request))
  (GET "/pick/:fromlang/:tolang" request (pick-subtitle request))
  (GET "/titles/:fromlang/:tolang/:letter" request (search/get-titles request))
  (GET "/alltext/:id" request (quiz/alltext request))
  (POST "/search/:fromlang/:tolang" request (search/query request)))

(defroutes secure-routes
  (GET "/review" request (review/review request))
  (GET "/logout" request (admin/logout request)))

