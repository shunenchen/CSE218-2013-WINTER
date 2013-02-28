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

(def playerIdMap {:BRAYDONCOBURN (uuid) :NICKLASGROSSMANN (uuid) :MIKEKNUBLE (uuid) :BRAYDENSCHENN (uuid) :HARRYZOLNIERCZYK (uuid) :SEANCOUTURIER (uuid) :WAYNESIMMONDS (uuid) :LUKESCHENN (uuid) :MATTREAD (uuid) :MAXIMETALBOT (uuid) :RUSLANFEDOTENKO (uuid) :BRUNOGERVAIS (uuid) :CLAUDEGIROUX (uuid) :ERIKGUSTAFSSON (uuid) :ZACRINALDO (uuid) :KIMMOTIMONEN (uuid) :DANNYBRIERE (uuid) :JAKUBVORACEK (uuid) :ILYABRYZGALOV (uuid) :BRIANBOUCHER (uuid) :MATTNISKANEN (uuid) :DERYKENGELLAND (uuid) :PAULMARTIN (uuid) :PASCALDUPUIS (uuid) :TANNERGLASS (uuid) :CHRISKUNITZ (uuid) :BRANDONSUTTER (uuid) :JAMESNEAL (uuid) :BEAUBENNETT (uuid) :MATTCOOKE (uuid) :CRAIGADAMS (uuid) :ROBERTBORTUZZO (uuid) :BROOKSORPIK (uuid) :JOEVITALE (uuid) :TYLERKENNEDY (uuid) :KRISLETANG (uuid) :EVGENIMALKIN (uuid) :SIDNEYCROSBY (uuid) :MARC-ANDREFLEURY (uuid) :TOMASVOKOUN (uuid) })
(def teamIdMap   {:NHL_PIT (uuid) :NHL_PHI (uuid)})

(defn create-test-team 
[]
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "INFO" :info (str {:name "Flyers" :id (tu-map :NHL_PIT) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "GAMES" :games (str [])}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "INFO" :info (str {:name "Flyers" :id (tu-map :NHL_PHI) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "GAMES" :games (str [])}))
)

(defn create-test-game 
  []
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "INFO" :info (str {:name "Flyers" :id (tu-map :NHL_PIT) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PIT) :data "GAMES" :games (str [])}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "INFO" :info (str {:name "Flyers" :id (tu-map :NHL_PHI) })}))
  (with-client client (put-item teamTable {:id (tu-map :NHL_PHI) :data "GAMES" :games (str [])}))
)
