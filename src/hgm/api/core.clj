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
          (catch java.lang.Throwable t# {:status 500 :body (.getMessage t#)}))))

;;;; Players

(defapi get-forwards
  "Get a list of all forwards in `team'."
  [team]
  (map :name (db/get-forwards team)))

(defapi get-defenders
  "Get a list of all defense in `team'."
  [team]
  (map :name (db/get-defenders team)))

(defapi get-goalies
  "Get a list of all goalies in `team'."
  [team]
  (map :name (db/get-goalies team)))

(defapi get-roster
  "Get the roster of `team'."
  [team]
  (map :name (db/get-roster team)))


;;;; Users

(defapi get-users
  "Get a list of all users in the system."
   []
  (db/get-users))

(defapi update-user
  "Update a user with some attributes."
  [user roles]
  (db/update-user user roles))


;;;; Events / Stats

(defapi create-game
  "Create a game between `home' and `away', starting at `startTime'."
  [startTime home away]
  (db/create-game startTime home away))

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
    (db/archive-game gameId (cons start events))))

(defapi get-events
  "Get a list of all game events for a particular game"
  [gameId]
  {:events (db/get-game-events gameId)})

(defapi get-game-stats
  "Get the stats for a specific game."
  [gameId]
  (if-let [summary (:summary (db/get-game gameId))]
    (assoc summary :done true)
    (let [summary (summarize-game gameId (db/get-game-events gameId))]
      (assoc summary :done false))))

(defapi get-player-career-stats
  "Get a list of all stats for this player
  stats currently supported
    * plus-minus"
  [playerId]
  (get-player-stats-internal playerId))

(defapi get-player-game-stats
  "Get a list of all stats for this player
  stats currently supported
    * plus-minus"
  [playerId gameId]
  (get-player-stats-internal playerId gameId))

;; the add-X-event functions are stubs and do not reflect an actual api...
(defapi add-start-game-event
  "Start a game."
  [gameId startTime home away]
  (if (or (not (db/unarchived-game-exists? gameId)) (db/game-started? gameId))
    (throw (Exception. "GAME ALREADY STARTED / NON-EXISTANT"))
    (do (db/add-game-event gameId 0 {:type :start
                                     :time startTime
                                     :home (:roster home)
                                     :away (:roster away)})
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
  (db/add-game-event gameId time {:type :end}))

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
