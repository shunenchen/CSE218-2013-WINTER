(ns hgm.core
  (:use compojure.core
        ring.middleware.json-response
        ring.util.response
        ring.adapter.jetty
        [clojure.core.incubator :only (-?>)]
        [environ.core :only [env]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.java.io :as io]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])
            [hgm.api.core :as api]
            [hgm.db.core :as db]))


(defroutes hgm-routes
  (GET  "/"      [] (io/file "resources/fanInterface.html"))
  (GET  "/admin" [] (friend/authorize #{:official} (io/file "resources/controlPanel.html")))
  (GET  "/game"  [] (friend/authorize #{:official} (io/file "resources/hockey.html")))
  (GET  "/games/new" [] (friend/authorize #{:official} (io/file "resources/game.html")))

  ;; team
  (GET  "/teams/:team"            [team] (api/get-team team))


  ;; searches
  (GET  "/teams"                  [] (api/search-teams ""))
  (GET  "/search/teams/:name"     [name] (api/search-teams name))
  (GET  "/search/players/:name"     [name] (api/search-players name))


  ;; roster
  (GET  "/teams/:team/get-forwards" [team] (api/get-forwards team))
  (GET  "/teams/:team/get-defense"  [team] (api/get-defenders  team))
  (GET  "/teams/:team/get-goalies"  [team] (api/get-goalies  team))
  (GET  "/teams/:team/get-roster"   [team] (api/get-roster   team))

  ;; players
  (GET  "/players/:playerId/stats"  [playerId] (api/get-player-career-stats playerId))
  (GET  "/players/:playerId/stats/for-game/:gameId" [playerId gameId]
        (api/get-player-game-stats playerId gameId))

  ;; games
  (GET  "/games"                    [] (api/get-games))
  (POST "/games"                    [startTime home away]
        (friend/authorize #{:official}
           (api/create-game (Integer. startTime) home away)))
  (GET  "/games/:gameId"            [gameId] (api/get-game gameId))
  (GET  "/games/:gameId/stats"      [gameId] (api/get-game-stats gameId))
  (GET  "/games/:gameId/events"     [gameId] (api/get-events gameId))
  (POST "/games/:gameId/archive"    [gameId]
        (friend/authorize #{:official}
           (api/archive-game gameId)))

  (GET  "/users"                    []
        (friend/authorize #{:official}
           (api/get-users)))
  (PUT  "/users/:user"              [user roles]
        (friend/authorize #{:official}
           (api/update-user user (into #{} (map keyword roles)))))

  (POST "/events/start-game"        [gameId startTime home away]
        (friend/authorize #{:official}
           (api/add-start-game-event gameId (Integer. startTime) home away)))

  (POST "/events/swap-players"      [gameId time outPlayer inPlayer]
        (friend/authorize #{:official}
           (api/add-swap-players-event gameId (Integer. time) outPlayer inPlayer)))

  (POST "/events/end-game"          [gameId time]
        (friend/authorize #{:official}
           (api/add-end-game-event gameId (Integer. time))))

  (POST "/events/shot"              [gameId time playerId]
        (friend/authorize #{:official}
           (api/add-shot-event gameId (Integer. time) playerId)))

  ; assists is a list of up to two player ids
  (POST "/events/goal"              [gameId time playerId assists]
        (friend/authorize #{:official}
           (api/add-goal-event gameId (Integer. time) playerId assists)))

  ; penalty is a map that includes penalty type, and all other relevant
  ; information for the penalty
  ; FIXME: hash out with Cam
  (POST "/events/penalty"              [gameId time playerId penalty type length]
        (friend/authorize #{:official}
           (api/add-penalty-event gameId (Integer. time) playerId penalty type (Integer. length))))


  (GET "/login" request (io/file "resources/login.html"))
  (friend/logout (ANY "/logout" request (redirect "/")))

  ;; FIXME: /view-openid and /echo-roles are only here for debugging purposes
  (GET "/view-openid" request
       (str "OpenId authentication? " (-?> request friend/identity friend/current-authentication pr-str)))
  (GET "/echo-roles" request (friend/authenticated
                              (-> (friend/current-authentication request)
                                  (select-keys [:roles])
                                  str)))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn check-creds
  [user]
  (merge user (or (db/get-user (:identity user))
                  (db/create-user user))))

(def app
  (-> hgm-routes
      (friend/authenticate {:workflows [(openid/workflow)]
                            :credential-fn check-creds})
      handler/site
      wrap-json-response))

(defn -main [& args]
  (let [port (Integer. (or (env :port) 8000))]
    (run-jetty app {:port port})))

