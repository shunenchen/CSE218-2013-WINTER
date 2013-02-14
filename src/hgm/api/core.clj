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
             :body {:data r#}})
          (catch java.lang.Throwable t# {:status 500 :body (.getMessage t#)}))))


(defapi get-forwards
  "Get a list of all forwards in `team'."
  [team]
  (db/get-forwards team))

(defapi get-defense
  "Get a list of all defense in `team'."
  [team]
  (db/get-defense team))

(defapi get-goalies
  "Get a list of all goalies in `team'."
  [team]
  (db/get-goalies team))

(defapi get-roster
  "Get the roster of `team'."
  [team]
  (db/get-roster team))

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
  [home away]
  (if-let [game (db/query home away)]
    {:gameId (:uuid game)}
    (let [gameId (db/create-game home away)]
      (db/add-gameEvents gameId 0 "on-ice")
      {:gameId gameId})))

(defapi add-swap-players-event
  "Swap two players during a game."
  [gameId time outPlayer inPlayer]
  (db/add-gameEvents gameId time))

(defapi add-end-game-event
  "End a game."
  [gameId time]
  (db/add-gameEvents gameId time "end"))

(defapi add-shot-event
  "Add a shot event."
  [gameId time player]
  (db/add-gameEvents gameId time "shot taken" player))
