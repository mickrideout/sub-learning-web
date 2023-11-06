(ns sub-learning-web.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [sub-learning-web.dev-middleware :refer [wrap-dev]]))

(def host-url "http://sublearning.com:3000")

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sub-learning-web started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sub-learning-web has shut down successfully]=-"))
   :middleware wrap-dev})
