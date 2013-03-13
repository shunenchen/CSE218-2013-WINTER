(ns hgm.api.core
  (:use hgm.api.internal)
  (:require [hgm.db.core :as db]))


(defn- name-vars
  [vars]
  (vec (for [v vars]
         [v (name v)])))

(defmacro defapi
  [name doc vars & body]
  `(defn ~name ~vars
     (try (let [r# (do (doseq [[v# s#] ~(name-vars vars)]
                         (if (nil? v#)
                           (throw (Exception. (str "missing argument: " s#)))))
                       ~@body)]
            {:status 200
             :body (if (map? r#) r# {:data r#})})
          (catch java.lang.Throwable t#
            (do (println (.getMessage t#))
                (.printStackTrace t#)
                {:status 500 :body (.getMessage t#)})))))

;;;; Players

(defapi get-forwards
  "Get a list of all forwards in `team'."
  [team]
  (filter #(= (:position %) :forward) (db/get-roster team)))

(defapi get-defenders
  "Get a list of all defense in `team'."
  [team]
  (filter #(= (:position %) :defender) (db/get-roster team)))

(defapi get-goalies
  "Get a list of all goalies in `team'."
  [team]
  (filter #(= (:position %) :goalie) (db/get-roster team)))

(defapi get-roster
  "Get the roster of `team'."
  [team]
  (db/get-roster team))


;;;;; Searches

(defapi search-teams
  "Get all teams that contain the substring `name'."
  [name]
  (db/search-teams name))

(defapi search-players
  "Get all players that contain the substring `name'."
  [name]
  (db/search-players name))

;;;; Users

(defapi get-users
  "Get a list of all users in the system."
   []
  (db/get-users))

(defapi update-user
  "Update a user with some attributes."
  [user roles]
  (db/update-user user roles))


;;;;;; games, teams

(defapi get-game
  "Gets the specified game"
  [gameId]
  (db/get-game gameId))

(defapi get-team
  "Gets the specified team"
  [team]
  (db/get-team-info team))

;;;; Events / Stats

(defapi create-game
  "Create a game between `home' and `away', starting at `startTime'."
  [startTime home away]
  (println (type startTime))
  (db/create-game {:startTime startTime :homeTeam home
      :awayTeam away :status "scheduled"}))

(defapi get-games
  "Get a list of all games, ignoring the game events."
  []
  (db/get-games))

(defapi archive-game
  "Archive `gameId'. Computes a full set of stats and stores them for later use.

  - game-stats per player
  - update player career stats
  - game summary"
  [gameId]
  (let [[start & events] (db/get-game-events gameId)
        player-ids (concat (:roster (:home start)) (:roster (:away start)))]
    (doseq [player-id player-ids]
      (let [stats (compute-player-stats player-id events)]
        (db/set-player-game-stats player-id gameId stats)
        (db/set-player-career-stats player-id
           (merge-with merge-stats
                       (db/get-player-career-stats player-id)
                       stats
                       {:game-ids gameId})))
      (db/set-game-summary gameId (summarize-game (cons start events))))
    (db/update-game
     (assoc-in (db/get-game gameId) [:status] "finalized"))))

(defapi get-events
  "Get a list of all game events for a particular game"
  [gameId]
  {:events (db/get-game-events gameId)})

;; (defapi get-game-stats
;;   "Get the stats for a specific game."
;;   [gameId]
;;   (if-let [summary (:summary (db/get-game gameId))]
;;     (assoc summary :done true)
;;     (let [summary (summarize-game (db/get-game-events gameId))]
;;       (assoc summary :done false))))

(defapi get-game-stats
  "Get the stats for a specific game."
  [gameId]
  {:startTime 500
   :home "4532f8ad-e638-43a3-8de7-b8d4f4b7845b"
   :away "46b24778-e521-4c76-b714-1c61450242ec"
   :goals [{:teamId "46b24778-e521-4c76-b714-1c61450242ec"
            :playerId "0dd8b326-ddf5-48cc-927d-9c361a0c5691"
            :type :goal
            :time 100}]
   :penalties []
   :done true})

;; (defapi get-player-career-stats
;;   "Get a list of all stats for this player
;;   stats currently supported
;;     * plus-minus"
;;   [playerId]
;;   (get-player-stats-internal playerId))

(defapi get-player-career-stats
  "Get a list of all stats for this player
  stats currently supported
    * plus-minus"
  [playerId]
  {:goals 2
   :assists 5
   :plusMinus -3
   :shots 7
   :hits 10})

;; (defapi get-player-game-stats
;;   "Get a list of all stats for this player
;;   stats currently supported
;;     * plus-minus"
;;   [playerId gameId]
;;   (get-player-stats-internal playerId gameId))

(defapi get-player-game-stats
  "Get a list of all stats for this player
  stats currently supported
    * plus-minus"
  [playerId gameId]
  {:goals 2
   :assists 5
   :plusMinus -3
   :shots 7
   :hits 10})

;; the add-X-event functions are stubs and do not reflect an actual api...
(defapi add-start-game-event
  "Start a game."
  [gameId startTime home away]
  (if (let [game (db/get-game gameId)]
       (or (nil? game) (not= "scheduled" (:status game))))
    (throw (Exception. "GAME ALREADY STARTED / NON-EXISTANT"))
    (do (db/add-game-event gameId 0 {:type :start
                                     :time startTime
                                     :home (:roster home)
                                     :away (:roster away)})
        (db/update-game
          (assoc-in (db/get-game gameId) [:status] "started"))
        (doseq [p (concat (:starting home) (:starting away))]
          (db/add-game-event gameId 0 {:type :enter-ice :playerId p})))))

(defapi add-swap-players-event
  "Swap two players during a game."
  [gameId time outPlayer inPlayer]
  (db/add-game-event gameId time
                    {:type :exit-ice :playerId outPlayer})
  (db/add-game-event gameId time
                    {:type :enter-ice :playerId inPlayer}))

(defapi add-end-game-event
  "End a game."
  [gameId time]
  (do (db/add-game-event gameId time {:type :end})
      (db/update-game
        (assoc-in (db/get-game gameId) [:status] "ended"))))

(defapi add-shot-event
  "Add a shot event."
  [gameId time playerId]
  (db/add-game-event gameId time {:type :shot :playerId playerId}))

(defapi add-goal-event
  "Add a goal event. Assist is a list of up to 2 playerIds who assisted"
  [gameId time playerId assists]
  (db/add-game-event gameId time {:type :goal :playerId playerId :assists assists}))

(defapi add-penalty-event
  "add a penalty for a particular player
  different penalty types will have different additional data"
  [gameId time playerId penalty]
  (db/add-game-event gameId time {:type :goal :playerId playerId :penalty penalty}))
