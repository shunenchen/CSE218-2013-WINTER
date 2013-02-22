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

(defapi get-users
  "Get a list of all users in the system."
   []
  (db/get-users))

(defapi get-events
  "Get a list of all game events for a particular game"
  [gameId]
  (db/get-game-events gameId))

(defapi get-player-career-stats
  "Get a list of all stats for this player
  stats currently supported
    * plus-minus"
  [player]
  (get-player-stats-internal player))


(defapi update-user
  "Update a user with some attributes."
  [user roles]
  (db/update-user user roles))

;;; the add-X-event functions are stubs and do not reflect an actual api...
(defapi add-start-game-event
  "Start a game."
  [gameId startTime homePlayers awayPlayers]
  (if (or (not (db/live-game-exists? gameId)) (db/game-running? gameId))
    (throw (Exception. "GAME ALREADY STARTED / NON-EXISTANT"))
    (do (db/add-game-event gameId 0 {:type :start :time startTime})
        (doseq [p (concat homePlayers awayPlayers)]
          (db/add-game-event gameId 0 {:type :on-ice :player p})))))

(defapi add-swap-players-event
  "Swap two players during a game."
  [gameId time outPlayer inPlayer]
  (db/add-game-event gameId time
                    {:type :off-ice :player outPlayer})
  (db/add-game-event gameId time
                    {:type :in-ice :player inPlayer}))

(defapi add-end-game-event
  "End a game."
  [gameId time]
  (db/add-game-event gameId time {:type :end}))

(defapi add-shot-event
  "Add a shot event."
  [gameId time player]
  (db/add-game-event gameId time {:type :shot :player player}))

(defapi add-goal-event
  "Add a goal event. Assist is a list of up to 2 playerIds who assisted"
  [gameId time player assists]
  (db/add-game-event gameId time {:type :goal :player player :assists assists}))

(defapi add-penalty-event
  "add a penalty for a particular player
  different penalty types will have different additional data"
  [gameId time player penalty]
  (db/add-game-event gameId time {:type :goal :player player :penalty penalty}))
