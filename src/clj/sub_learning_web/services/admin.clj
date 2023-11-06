(ns sub-learning-web.services.admin
  (:require [sub-learning-web.layout :as layout]
            [ring.util.http-response :as response]
            [sub-learning-web.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-status :as status]
            [sub-learning-web.services.validation :as v]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [ring.util.response :refer [redirect content-type]]))


(defn login-page [request]
  (layout/render "login.html"
    (merge (:flash request) (:params request))))

(defn signup-page [params]
  (layout/render "signup.html" params))

(defn register-user! [user]
  "Add a new user"
  (try
    (db/add-user!
      (-> user
          (dissoc :confirmation)
          (update :password hashers/encrypt)))
    (-> (response/found "/")
        (assoc :session {:identity (:id user)})
        (assoc :flash {:user-message "Account successfully created"}))
    (catch Exception e
      (log/error e)
      (response/internal-server-error
        {:result :error :message "server error while adding user"}))))

(defn signup-page-submit [{:keys [params session]}] ;get params key from request map
  (if-let [errors (v/validate-signup? params)]
    (assoc (response/found "/signup") :flash (assoc params :errors errors))
    (do
      (register-user! params))))

(defn decode-auth [encoded]
  (let [auth (second (.split encoded " "))]
    (-> (.decode (java.util.Base64/getDecoder) auth)
      (String. (java.nio.charset.Charset/forName "UTF-8"))
      (.split ":"))))

(defn authenticate-user [[email password]]
  (when-let [user (db/get-user {:email email})]
    (when (hashers/check password (:password user))
      user)))


(defn login-authenticate [request]
    (let [email (get-in request [:params :email])
          password (get-in request [:params :password])
          session (:session request)
          next (get-in request [:params :next])]
      (if-let [user (authenticate-user [email password])]
        (-> (redirect (if-not (empty? next)
                    next
                    "/"))
             (assoc-in [:session :identity] (:id user)))
        (-> (redirect "/login")
            (assoc-in [:flash :user-message] "Login incorrect, please try again")))))

(defn unauthorized-handler
  [request metadata]
  (cond
    ;; If request is authenticated, raise 403 instead
    ;; of 401 (because user is authenticated but permission
    ;; denied is raised).
    (authenticated? request)
    (-> (layout/render "error.html" request)
        (assoc :status 403))
    ;; In other cases, redirect the user to login page.
    :else
    (let [current-url (:uri request)]
      (redirect (format "/login?next=%s" current-url)))))

;; Create an instance of auth backend.

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn logout
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (-> (redirect "/")
        (assoc :session {})
        (assoc-in [:flash :user-message] "Successfully logged out"))))

(defn contact [request]
  (layout/render "contact.html"
    {:session (:session request)}))

(defn contact-submission [{:keys [params session]}]
  (if-let [errors (v/validate-contact? params)]
    (assoc (response/found "/contact" :flash (assoc params :errors errors)))
    (do
      (db/create-message! params)
      (-> (response/found "/")
        (assoc :flash {:user-message "Thank you for your message. We will endeavour to reply as soon as possible"})))))




