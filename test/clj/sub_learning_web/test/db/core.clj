(ns sub-learning-web.test.db.core
  (:require [sub-learning-web.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [sub-learning-web.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'sub-learning-web.config/env
      #'sub-learning-web.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/add-user!
               t-conn
               {:email      "sam.smith@example.com"
                :password       "pass"})))
    (is (= {:email      "sam.smith@example.com"
            :password       "pass"}
           (select-keys (db/get-user t-conn {:email "sam.smith@example.com"}) [:email :password])))))
