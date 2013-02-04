(ns hgm.core
  (:use compojure.core
        ring.middleware.json-response
        ring.util.response
        ring.adapter.jetty
        [environ.core :only [env]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.java.io :as io]
            [hgm.api.core :as api]))

(defroutes hgm-routes
  (GET "/" [] (io/file "resources/hockey.html"))
  (GET "/teams/:team/get-forwards" [team] (api/get-forwards team))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> hgm-routes
      handler/site
      wrap-json-response))

(defn -main [& args]
  (let [port (Integer. (or (env :port) 8000))]
    (run-jetty app {:port port})))
