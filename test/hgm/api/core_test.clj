(ns hgm.api.core_test
  (:use clojure.test
        hgm.api.core
        hgm.api.internal)
  (:require [hgm.db.core :as db]))

;; FIXME: Need more tests, particularly goals and plus-minus stats
(deftest player-career-stats
  (let [stats {:goals 5 :plus-minus -3 :assists 3}]
    (with-redefs [db/get-player-career-stats
                  (fn [player]
                    stats)]
      (is (= stats (get-player-stats-internal "john"))))))

