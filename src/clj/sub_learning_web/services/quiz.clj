(ns sub-learning-web.services.quiz
  (:require [sub-learning-web.layout :as layout]
            [ring.util.http-response :as response]
            [sub-learning-web.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-status :as status]
            [ring.util.codec :as codec]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [clojure.string :as str]
            [sub-learning-web.env :as env]))

(def line-count-default 3)

(defn parse-int [s]
  (Integer/parseInt s))

(defn update-vals [map vals f]
  (reduce #(update-in % [%2] f) map vals))

(defn conditional-str-concat [a b]
  "If string a differs from string b then concat then otherwise return a"
  (if (= a b)
    a
    (str a b)))

(defn find-first [f coll]
  (first (filter f coll)))

(defn consolidate-subs [subs]
  (apply merge-with conditional-str-concat subs))

(defn reverse-subtitle-language? [fromlang tolang sub-link-id]
  "Check the desire from / to language against the sub-link entry and determine if reversal is needed"
    (let [subdetails (db/get-sub-link-entry {:id sub-link-id})
     from-lang-id (:fromlang subdetails)
     sub-link-from-lang-name (:name (db/get-language-by-id {:id from-lang-id}))]
      (not (= fromlang sub-link-from-lang-name))))

(defn get-next-sub-id [idmap rev linecount]
  "Get the next id string given the current sub and line id"
  (let [dblookupmap (last (db/get-next-line-id (merge idmap {:linecount linecount})))]
    (if (empty? dblookupmap)
      (let [subdetails (db/get-sub-link-entry idmap)
            from-lang-id (:fromlang subdetails)
            to-lang-id (:tolang subdetails)
            from-lang-name (:name (db/get-language-by-id {:id from-lang-id}))
            to-lang-name (:name (db/get-language-by-id {:id to-lang-id}))]
        (if (= "true" rev)
          (str "/quiz/" to-lang-name "/" from-lang-name)
          (str "/quiz/" from-lang-name "/" to-lang-name)))
      (if (= "true" rev)
        (str "/sub/" (:id idmap) "/" (:lineid dblookupmap) "/true")
        (str "/sub/" (:id idmap) "/" (:lineid dblookupmap))))))

(defn review-string-to-map [save-string]
  (let [[user-id-str subid-str lineid-str rev-str] (str/split save-string #"_")
        tmp-map {:userid user-id-str :id subid-str :lineid lineid-str :rev (boolean (Boolean/valueOf rev-str))}
        query-map (update-vals tmp-map [:userid :id :lineid] parse-int)]
    query-map))

(defn save-for-review [save-string]
  "Save a subtitle entry for review"
    (db/save-review! (review-string-to-map save-string)))

(defn create-save-string [{:keys [userid id lineid rev] :or {rev false}}]
  "Create the url string to save a review"
  (apply str (interpose "_" [userid id lineid rev])))

(defn get-reverse-url [id lineid rev]
  "Get the reverse url of the current url"
  (if (= "true" rev)
    (str "/sub/" id "/" lineid)
    (str "/sub/" id "/" lineid "/true")))

(defn get-line-ids [link-lines linekey]
  "Given a line key (:fromlines :tolines) return the lineids"
  (flatten (for [entry link-lines :let [lineids (linekey entry)]] lineids)))

(defn merge-link-lines-with-sub-lines [link-lines from-lines to-lines]
  "Manually link the link lines with the sub lines to produce the map for the sub quiz screen"
  )

(defn key-lines-by-lineid [linesvec]
  "Flattens a vector of line maps into one map keyed by lineid"
  (into {} (map #(hash-map (:lineid %) %) linesvec)))

(defn concat-vec-lineids [lineids lookupmap]
  "Given a vector of lineids, return the concatenated content for those lines"
  (apply str (map #(:content (get lookupmap %)) lineids)))

(defn replace-lineids-with-content [link-lines from-line-ids to-line-ids]
  "Replace the link line map from/to lines with the actual content"
  (map #(-> (update-in % [:fromlines] concat-vec-lineids from-line-ids)
            (update-in [:tolines] concat-vec-lineids to-line-ids)
            ) link-lines))

(defn number-of-lines-to-display [request]
  "Returns the configured linecount of lines to display. If config not found default"
  (let [param-vec [(get-in request [:params :lines]) (get-in request [:cookies "lines" :value]) (str line-count-default)]
        linecount-param (parse-int (find-first #(not (nil? %)) param-vec))]
    (if (>= 6 linecount-param 1)
      linecount-param
      line-count-default)))

(defn get-quiz-elements [req-data]
  "Return a map of elements to quiz the user"
  (let [link-data (db/get-sub-link-entry req-data)
        link-lines (db/get-sub-lines req-data)
        from-line-ids (key-lines-by-lineid (db/get-lines-in {:lineids (get-line-ids link-lines :fromlines) :id (:fromid link-data)}))
        to-line-ids (key-lines-by-lineid (db/get-lines-in {:lineids (get-line-ids link-lines :tolines) :id (:toid link-data)}))
        processed-lines (replace-lineids-with-content link-lines from-line-ids to-line-ids)
        from-lines (vec (map :fromlines processed-lines))
        to-lines (vec (map :tolines processed-lines))
        title (db/get-sub-title req-data)
        rev (:rev req-data)]
    (if (or (= "true" rev) (= true rev))
      {:fromlines to-lines
       :tolines from-lines
       :title title}
       {:fromlines from-lines
       :tolines to-lines
       :title title})))

(defn sub-page [request]
  "Main page to handle subtitle quizes"
  (let [{:keys [id lineid save rev]} (:params request)
        real-line-id (if (= "first" lineid)
                       (str (:lineid (db/get-first-line-id {:id (parse-int id)})))
                       lineid)
        query-params (-> (merge (:params request) {:lineid real-line-id}
                                {:userid (get-in request [:session :identity])}
                                {:linecount (number-of-lines-to-display request)})
                         (update-vals [:id :lineid] parse-int))
        user-uuid (get-in request [:cookies "uuid" :value] (str (java.util.UUID/randomUUID)))
        line-count (number-of-lines-to-display request)]
     (if (and (authenticated? (:session request)) (not (nil? save)))
       (save-for-review save))
    (let [quiz-elements (get-quiz-elements query-params)
          next-page (get-next-sub-id query-params rev line-count)
          resp-map {:next next-page
         :id {:id (:id query-params)}
         :rev (if (nil? (:rev query-params))
                "false"
                (:rev query-params))
         :session (:session request)
         :save (create-save-string query-params)
         :hosturl env/host-url
         :revurl (get-reverse-url id real-line-id rev)}]
      (-> (layout/render
          "sub.html"
          (merge quiz-elements resp-map))
          (merge {:cookies {"next-page" {:value next-page :max-age 2592000 :path "/"}
                            "uuid" {:value user-uuid :max-age 2147483647 :path "/"}
                            "lines" {:value line-count :max-age 2147483647 :path "/"}}})))))

;;; start quiz languages
(defn random-subtitle [fromlang tolang]
  "Given a string of two languages (eg: english-german) redirect to a random subtitle for that pair"
  (let [langmap {:fromlang fromlang :tolang tolang}
        id (:id (db/get-random-sub-id langmap))
        to-reverse? (reverse-subtitle-language? fromlang tolang id)]
    (if (nil? id)
      (response/not-found)
      (if to-reverse?
        (response/found (str "/sub/" id "/first/true"))
        (response/found (str "/sub/" id "/first"))))))

;; get language pairs
(defn get-language-pairs [request]
  "Given an input language find all paired langauges"
  (let [language (get-in request [:params :language])]
    (layout/render
      "pairs.html"
      {:pairs (db/get-language-pairs {:lang language})
       :leftlang language
       :session (:session request)})))

(defn redirect-to-pick [request]
  "Redirect to a new movie based on the current subtitle"
  (let [params (:params request)
        subinfo (db/get-sub-link-entry {:id (parse-int (:id params))})
        fromlangid (:fromlang subinfo)
        tolangid (:tolang subinfo)
        fromlanginfo (db/get-language-by-id {:id fromlangid})
        tolanginfo (db/get-language-by-id {:id tolangid})]
    (if (= "false" (:rev params))
      (response/found (str "/pick/" (:name fromlanginfo) "/" (:name tolanginfo)))
      (response/found (str "/pick/" (:name tolanginfo) "/" (:name fromlanginfo))))))

(defn alltext [request]
  "Return both from and two lines as text"
  (let [id (parse-int (get-in request [:params :id]))
        quiz-elements (get-quiz-elements {:id id :lineid 1 :linecount 10000})
        title (db/get-sub-title {:id id})]
    (-> (layout/render-text
         "alltext.html"
         {:lines (interleave (:fromlines quiz-elements) (:tolines quiz-elements))
          :title title}))))


