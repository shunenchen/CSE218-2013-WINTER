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

(def users (atom {}))

(defn get-users
  []
  (vec (vals @users)))

(defn get-user
  [id]
  (@users id))

(defn create-user
  [m]
  ((swap! users assoc (:identity m)
          (assoc m :roles #{:official}))
   (:identity m)))

(defn update-user
  "FIXME: do something useful"
  [user roles]
  ((swap! users update-in [user] assoc :roles roles)
   user))

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
