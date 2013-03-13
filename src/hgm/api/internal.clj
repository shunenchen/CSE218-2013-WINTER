(ns hgm.api.internal
  (:require [hgm.db.core :as db]))


(defn update-goal-stats
  [stats player event]
  (if (= (:id player) (:playerId event))
    (update-in stats [:goals] inc)
    stats))

(defn update-plus-minus-stats
  [stats player event]
  (if (:on-ice stats)
    (if (= (:teamId player) (:teamId event))
      (update-in stats [:plus-minus] inc)
      (update-in stats [:plus-minus] dec))
    stats))

(defn exit-ice
  [stats player event]
   (if (= (:playerId event) (:id player))
     (-> stats
         (assoc :on-ice false)
         (update-in [:time-on-ice] #(+ % (- (:time event) (:last-entered stats)))))
     stats))

(defn enter-ice
  [stats player event]
  (if (= (:playerId event) (:id player))
    (assoc stats :on-ice true
                 :last-entered (:time event))
    stats))

(defn penalty
  [stats player event]
  (if (= (:playerId event) (:id player))
    (update-in stats [:penalties] inc)
    stats))

(defn shot
  [stats player event]
  (if (= (:playerId event) (:id player))
    (update-in stats [:shots] inc)
    stats))

(defn compute-player-stats
  [player events]
  (let [stats (reduce
               (fn [stats event]
                 (case (:type event)
                   :goal (-> stats
                             (update-goal-stats player event)
                             (update-plus-minus-stats player event))
                   :enter-ice (enter-ice stats player event)
                   :exit-ice (exit-ice stats player event)
                   :penalty (penalty stats player event)
                   :shot (shot stats player event)
                   :start stats
                   ))
               {:on-ice false
                :goals 0
                :penalties 0
                :plus-minus 0
                :shots 0
                :time-on-ice 0}
               events)]
    (dissoc stats :on-ice :last-entered)))

(defn get-player-stats-internal
  ([player]
     (db/get-player-career-stats player))
  ([player game]
     (if (not= "finalized" (:status (db/get-game game)))
       (let [events (db/get-game-events game)]
         (compute-player-stats (db/get-player player) events))
       (db/get-player-game-stats player game))))

(defn merge-stats
  [old new]
  (if (vector? old)
    (conj old new)
    (+ old new)))

(defn summarize-game
  [[start & events]]
  {:startTime (:startTime start)
   :home (:teamId (:home start))
   :away (:teamId (:away start))
   :goals (filter #(= :goal (:type %)) events)
   :penalties (filter #(= :penalty (:type %)) events)})
