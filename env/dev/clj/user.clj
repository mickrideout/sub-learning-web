(ns user
  (:require [mount.core :as mount]
            sub-learning-web.core))

(defn start []
  (mount/start-without #'sub-learning-web.core/http-server
                       #'sub-learning-web.core/repl-server))

(defn stop []
  (mount/stop-except #'sub-learning-web.core/http-server
                     #'sub-learning-web.core/repl-server))

(defn restart []
  (stop)
  (start))


