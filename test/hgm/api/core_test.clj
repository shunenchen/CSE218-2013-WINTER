(ns hgm.api.core-test
  (:use clojure.test
        hgm.api.core
        hgm.api.internal)
  (:require [hgm.db.core :as db]))

(def x {:id "XXXX" :team "bar"})
(def y {:id "YYYY" :team "bar"})
(def z {:id "ZZZZ" :team "baz"})

(defn event-fixture [f]
  (with-redefs [db/live-game-exists? (fn [_] true)
                db/get-game-events
                (fn [_]
                  [{:type :enter-ice  :player (:id x)                 :time 0}
                   {:type :enter-ice  :player (:id y)                 :time 40}
                   {:type :goal       :player (:id x) :team (:team x) :time 50}
                   {:type :enter-ice  :player (:id z)                 :time 65}
                   {:type :exit-ice   :player (:id y)                 :time 90}
                   {:type :goal       :player (:id x) :team (:team x) :time 100}
                   {:type :enter-ice  :player (:id y)                 :time 150}
                   {:type :goal       :player (:id x) :team (:team x) :time 160}
                   {:type :exit-ice   :player (:id x)                 :time 200}
                   {:type :exit-ice   :player (:id y)                 :time 200}
                   {:type :exit-ice   :player (:id z)                 :time 200}])]
    (f)))

(use-fixtures :once event-fixture)

(deftest player-career-stats
  (let [stats {:goals 5 :plus-minus -3 :assists 3}]
    (with-redefs [db/get-player-career-stats
                  (fn [player]
                    stats)]
      (is (= stats (get-player-stats-internal x))))))

(deftest player-game-stats-respects-off-ice
  (let [stats (get-player-stats-internal y "game")]
    (println stats)
    (is (= 0 (:goals stats)))
    (is (= 2 (:plus-minus stats)))))

(deftest player-game-stats-not-involved
  (let [stats (get-player-stats-internal {:id "not player" :team "bar"} "game")]
    (is (= 0 (:goals stats)))
    (is (= 0 (:plus-minus stats)))))

(deftest player-game-stats-opposing-team
  (let [stats (get-player-stats-internal z "game")]
    (is (= 0 (:goals stats)))
    (is (= -2 (:plus-minus stats)))))

(deftest player-game-stats-goal
  (let [stats (get-player-stats-internal x "game")]
    (is (= 3 (:goals stats)))
    (is (= 3 (:plus-minus stats)))))

