(ns sub-learning-web.env
  (:require [clojure.tools.logging :as log]))

(def host-url "http://sublearning.com")

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sub-learning-web started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sub-learning-web has shut down successfully]=-"))
   :middleware identity})
