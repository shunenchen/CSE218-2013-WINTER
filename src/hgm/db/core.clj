(ns hgm.db.core)

;;; DB CODE GOES HERE
(defn get-forwards
  "FIXME: do something useful"
  [team]
  (apply concat (repeat 4 ["John" "Eric" "Ben"])))

(defn get-defense
  "FIXME: do something useful"
  [team]
  (apply concat (repeat 3 ["Sam" "Cam"])))

(defn get-goalies
  "FIXME: do something useful"
  [team]
  ["David" "David"])

(defn get-roster
  "FIXME: do something useful"
  [team]
  (concat (get-forwards team) (get-defense team) (get-goalies team)))

(defn get-users
  []
  ["Eric" "Ben"])

(defn update-user
  "FIXME: do something useful"
  [user roles]
  [user roles])

(defn add-gameEvents
  "FIXME: do something useful"
  [gameId time & rest]
  {:gameId gameId
   :time time
   :rest rest})

(defn create-game
  "FIXME: do something useful"
  [home away]
  "XXXX-XXXX-XXXX-XXXX")

(defn query
  "FIXME: do something useful"
  [home away]
  (if (= home "foo")
    {:uuid "YYYY-YYYY-YYYY-YYYY"}))
