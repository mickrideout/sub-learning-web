(ns sub-learning-web.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [sub-learning-web.layout :refer [error-page]]
            [sub-learning-web.routes.home :refer [home-routes secure-routes]]
            [compojure.route :as route]
            [sub-learning-web.env :refer [defaults]]
            [mount.core :as mount]
            [sub-learning-web.middleware :as middleware]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [sub-learning-web.services.admin :as admin]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'secure-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats)
        (wrap-routes wrap-authorization admin/auth-backend)
        (wrap-routes wrap-authentication admin/auth-backend))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
