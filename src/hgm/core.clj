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
  (GET  "/" [] (io/file "resources/hockey.html"))
  (GET  "/teams/:team/get-forwards" [team] (api/get-forwards team))
  (GET  "/teams/:team/get-defense"  [team] (api/get-defense  team))
  (GET  "/teams/:team/get-goalies"  [team] (api/get-goalies  team))
  (GET  "/teams/:team/get-roster"   [team] (api/get-roster   team))

  (GET  "/users"                    []     (api/get-users))
  (PUT  "/users/:user"              [user roles]
        (api/update-user user roles))

  (POST "/events/start-game"        [home away]
        (api/add-start-game-event home away))

  (POST "/events/swap-players"      [gameId time outPlayer inPlayer]
        (api/add-swap-players-event gameId time outPlayer inPlayer))

  (POST "/events/end-game"          [gameId time]
        (api/add-end-game-event gameId time))

  (POST "/events/shot"              [gameId time player]
        (api/add-shot-event gameId time player))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> hgm-routes
      handler/site
      wrap-json-response))

(defn -main [& args]
  (let [port (Integer. (or (env :port) 8000))]
    (run-jetty app {:port port})))
