(ns hgm.api.core
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


(defapi get-forwards-names
  "Get a list of all forwards in `team'."
  [team]
  (db/get-forwards-names team))

(defapi get-defenders-names
  "Get a list of all defense in `team'."
  [team]
  (db/get-defenders-names team))

(defapi get-goalies-names
  "Get a list of all goalies in `team'."
  [team]
  (db/get-goalies-names team))

(defapi get-roster-names
  "Get the roster of `team'."
  [team]
  (db/get-roster-names team))

(defapi get-users
  "Get a list of all users in the system."
  []
  (db/get-users))

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
    (do (db/add-gameEvent gameId 0 {:type :start :time startTime})
    	(doseq [p (concat homePlayers awayPlayers)]
          (db/add-gameEvent gameId 0 {:type :on-ice :player p})))))

(defapi add-swap-players-event
  "Swap two players during a game."
  [gameId time outPlayer inPlayer]
  (db/add-gameEvent gameId time
                    {:type :off-ice :player outPlayer})
  (db/add-gameEvent gameId time
                    {:type :in-ice :player inPlayer}))

(defapi add-end-game-event
  "End a game."
  [gameId time]
  (db/add-gameEvent gameId time {:type :end}))

(defapi add-shot-event
  "Add a shot event."
  [gameId time player]
  (db/add-gameEvent gameId time {:type :shot :player player}))
