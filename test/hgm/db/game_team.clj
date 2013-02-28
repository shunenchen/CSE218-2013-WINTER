(ns hgm.db.game-team
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.test)
  (:use hgm.db.core :reload)
  (:use hgm.db.clj_dynamo :reload))

(defn delete-all-team []
   (with-client client 
   (doseq [item (scan teamTable {})]
   (delete-item teamTable (create-key (:id item) (:data item))))
   (is (empty? (scan teamTable {}))))
)

(defn delete-all-game []
   (with-client client 
   (doseq [item (scan gameTable {})]
   (delete-item gameTable (create-key (:id item) )))
   (is (empty? (scan gameTable {}))))
)

(defn get-uuid-map 
	[]
	(with-client client (get-item playerTable (create-key "0"))))
	  
(def pu-map (read-string ( (get-uuid-map) :playerHashList)))
(def tu-map (read-string ( (get-uuid-map) :teamHashList)))

(defn create-test-team 
  []
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "INFO" :info (str {:name "Penguins" :id (tu-map :NHL_PIT) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "GAMES" :games (str [])}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "INFO" :info (str {:name "Flyers" :id (tu-map :NHL_PHI) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "GAMES" :games (str [])}))
)

(defn create-test-game 
  []
  (create-game {:startTime 1361388600 :awayTeam (tu-map :NHL_PIT) :homeTeam (tu-map :NHL_PHI)}))
