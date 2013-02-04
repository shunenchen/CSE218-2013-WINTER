(ns hgm.core
  (:use compojure.core
        ring.adapter.jetty
        [environ.core :only [env]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.java.io :as io]))

(defroutes hgm-routes
  (GET "/" [] (io/file "resources/hockey.html"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site hgm-routes))

(defn -main [& args]
  (let [port (Integer. (or (env :port) 8000))]
    (run-jetty app {:port port})))
