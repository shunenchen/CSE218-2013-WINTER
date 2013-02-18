(ns hgm.api.internal
  (:require [hgm.db.core :as db]))


(defn update-player-goal-stats
  [stats player event]
  (if (= player (:player event))
    (update-in stats [:goals] inc)
    stats))

(defn update-player-plus-minus-stats
  [stats player event]
  (if (:on-ice stats)
    (if (= (:team player) (:team event))
      (update-in stats [:plus-minus] inc)
      (update-in stats [:plus-minus] dec))
    stats))

(defn compute-player-stats
  [player events]
  (let [stats (reduce
               (fn [stats event]
                 (case (:type event)
                   :goal (-> stats
                             (update-player-goal-stats player event)
                             (update-player-plus-minus-stats player event))
                   ))
               {:on-ice false}
               events)]
    (dissoc stats :on-ice)))

(defn get-player-stats-internal
  ([player]
     (db/get-player-career-stats player))
  ([player game]
     (if (db/live-game-exists? game)
       (let [events (db/get-game-events game)]
         (compute-player-stats player events))
       (db/get-player-game-stats player game))))
