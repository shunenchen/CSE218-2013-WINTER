(ns hgm.api.core-test
  (:use clojure.test
        hgm.api.core
        hgm.api.internal)
  (:require [hgm.db.core :as db]))

(def x {:id "XXXX" :teamId "bar"})
(def y {:id "YYYY" :teamId "bar"})
(def z {:id "ZZZZ" :teamId "baz"})
(def events
  [{:type :enter-ice  :playerId (:id x)                     :time 0}
   {:type :enter-ice  :playerId (:id y)                     :time 40}
   {:type :goal       :playerId (:id x) :teamId (:teamId x) :time 50}
   {:type :enter-ice  :playerId (:id z)                     :time 65}
   {:type :exit-ice   :playerId (:id y)                     :time 90}
   {:type :goal       :playerId (:id x) :teamId (:teamId x) :time 100}
   {:type :enter-ice  :playerId (:id y)                     :time 150}
   {:type :goal       :playerId (:id x) :teamId (:teamId x) :time 160}
   {:type :exit-ice   :playerId (:id x)                     :time 200}
   {:type :exit-ice   :playerId (:id y)                     :time 200}
   {:type :exit-ice   :playerId (:id z)                     :time 200}])


(deftest player-career-stats
  (let [stats {:goals 5 :plus-minus -3 :assists 3}]
    (with-redefs [db/get-player-career-stats
                  (fn [player]
                    stats)]
      (is (= stats (get-player-stats-internal x))))))

(deftest player-game-stats-respects-off-ice
  (let [stats (compute-player-stats y events)]
    (is (= 0 (:goals stats)))
    (is (= 2 (:plus-minus stats)))
    (is (= 100 (:time-on-ice stats)))))

(deftest player-game-stats-not-involved
  (let [stats (compute-player-stats {:id "not player" :team "bar"} events)]
    (is (= 0 (:goals stats)))
    (is (= 0 (:plus-minus stats)))
    (is (= 0 (:time-on-ice stats)))))

(deftest player-game-stats-opposing-team
  (let [stats (compute-player-stats z events)]
    (is (= 0 (:goals stats)))
    (is (= -2 (:plus-minus stats)))
    (is (= 135 (:time-on-ice stats)))))

(deftest player-game-stats-goal
  (let [stats (compute-player-stats x events)]
    (is (= 3 (:goals stats)))
    (is (= 3 (:plus-minus stats)))
    (is (= 200 (:time-on-ice stats)))))

