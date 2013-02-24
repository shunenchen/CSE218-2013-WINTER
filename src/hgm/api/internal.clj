(ns hgm.api.internal
  (:require [hgm.db.core :as db]))


(defn update-player-goal-stats
  [stats player event]
  (if (= (:id player) (:player event))
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
                   :on-ice (if (= (:player event) (:id player))
                             (assoc stats :on-ice true)
                             stats)
                   :off-ice (if (= (:player event) (:id player))
                              (assoc stats :on-ice false)
                              stats)
                   ))
               {:on-ice false
                :goals 0
                :plus-minus 0}
               events)]
    (dissoc stats :on-ice)))

(defn get-player-stats-internal
  ([player]
     (db/get-player-career-stats player))
  ([player game]
     (if (db/unarchived-game-exists? game)
       (let [events (db/get-game-events game)]
         (compute-player-stats (db/get-player player) events))
       (db/get-player-game-stats player game))))
