(ns sub-learning-web.services.search
  (:require [sub-learning-web.layout :as layout]
            [ring.util.http-response :as response]
            [sub-learning-web.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-status :as status]
            [ring.util.codec :as codec]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [clojure.string :as str]
            [sub-learning-web.env :as env]))

(defn get-titles [request]
  (let [params (:params request)
        letter (:letter params)
        tolangid (:id (db/get-language-by-name {:name (:tolang params)}))
        fromlangid (:id (db/get-language-by-name {:name (:fromlang params)}))
        titles (db/get-titles (merge {:fromlangid fromlangid :tolangid tolangid} (select-keys params [:id :letter])))
        titles-rev (map #(merge % {:rev "true"}) (db/get-titles (merge {:fromlangid tolangid :tolangid fromlangid} (select-keys params [:id :letter]))))]
    (layout/render
      "titles.html"
      {:titles (concat titles titles-rev)})))

(defn query [request]
  (let [params (:params request)
        search (:search params)
        tolangid (:id (db/get-language-by-name {:name (:tolang params)}))
        fromlangid (:id (db/get-language-by-name {:name (:fromlang params)}))
        results (db/search-titles (merge {:fromlangid fromlangid :tolangid tolangid} (select-keys params [:id :search])))
        results-rev (map #(merge % {:rev "true"}) (db/search-titles (merge {:fromlangid tolangid :tolangid fromlangid} (select-keys params [:id :search]))))]
    (layout/render
      "search-results.html"
      {:titles (concat results results-rev)})))


