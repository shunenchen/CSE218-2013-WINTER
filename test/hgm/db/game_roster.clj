(ns hgm.db.game-roster
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.test)
  (:use hgm.db.core :reload)
  (:use hgm.db.clj_dynamo :reload))
  
(defn delete-all-players []
   (with-client client 
   (doseq [item (scan playerTable {})]
   (delete-item playerTable (create-key (:uuid item) (:date-time-uuid item))))
   (is (empty? (scan playerTable {}))))
)

(def playerIdMap {:BRAYDONCOBURN (uuid) :NICKLASGROSSMANN (uuid) :MIKEKNUBLE (uuid) :BRAYDENSCHENN (uuid) :HARRYZOLNIERCZYK (uuid) :SEANCOUTURIER (uuid) :WAYNESIMMONDS (uuid) :LUKESCHENN (uuid) :MATTREAD (uuid) :MAXIMETALBOT (uuid) :RUSLANFEDOTENKO (uuid) :BRUNOGERVAIS (uuid) :CLAUDEGIROUX (uuid) :ERIKGUSTAFSSON (uuid) :ZACRINALDO (uuid) :KIMMOTIMONEN (uuid) :DANNYBRIERE (uuid) :JAKUBVORACEK (uuid) :ILYABRYZGALOV (uuid) :BRIANBOUCHER (uuid) :MATTNISKANEN (uuid) :DERYKENGELLAND (uuid) :PAULMARTIN (uuid) :PASCALDUPUIS (uuid) :TANNERGLASS (uuid) :CHRISKUNITZ (uuid) :BRANDONSUTTER (uuid) :JAMESNEAL (uuid) :BEAUBENNETT (uuid) :MATTCOOKE (uuid) :CRAIGADAMS (uuid) :ROBERTBORTUZZO (uuid) :BROOKSORPIK (uuid) :JOEVITALE (uuid) :TYLERKENNEDY (uuid) :KRISLETANG (uuid) :EVGENIMALKIN (uuid) :SIDNEYCROSBY (uuid) :MARC-ANDREFLEURY (uuid) :TOMASVOKOUN (uuid) })
(def teamIdMap   {:NHL_PIT (uuid) :NHL_PHI (uuid)})

(defn create-player-uuid
  "Creates a player. player is a map which must include:
   :teamId
   :position
   Returns the stored player object with an associated :id key."
  [player puuid]
    (let [p (assoc player :id puuid)]
      (with-client client
        (put-item playerTable
          {:id (:id p)
           :info (str p)})
        (put-item teamPlayerTable
          {:teamId (:teamId p)
           :playerId (:id p)
           :info (str p)}))
      p))
	  
                                                                                                                                                 
(defn add-all-players                                                                                                                            
  []                                                                                                                                             
 (create-player-uuid {:name "BRAYDON COBURN"		 :position :defender	 :number 5	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :BRAYDONCOBURN))
  (create-player-uuid {:name "NICKLAS GROSSMANN"	 :position :defender	 :number 8	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :NICKLASGROSSMANN)) 
  (create-player-uuid {:name "MIKE KNUBLE"			 :position :forward		 :number 9	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :MIKEKNUBLE))
  (create-player-uuid {:name "BRAYDEN SCHENN"		 :position :forward		 :number 10	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :BRAYDENSCHENN))
  (create-player-uuid {:name "HARRY ZOLNIERCZYK"	 :position :forward		 :number 12	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :HARRYZOLNIERCZYK))
  (create-player-uuid {:name "SEAN COUTURIER"		 :position :forward		 :number 14	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :SEANCOUTURIER))
  (create-player-uuid {:name "WAYNE SIMMONDS"		 :position :forward		 :number 17	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :WAYNESIMMONDS))
  (create-player-uuid {:name "LUKE SCHENN"			 :position :defender	 :number 22	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :LUKESCHENN))
  (create-player-uuid {:name "MATT READ"		     :position :forward		 :number 24	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :MATTREAD))
  (create-player-uuid {:name "MAXIME TALBOT"		 :position :forward		 :number 25	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :MAXIMETALBOT))
  (create-player-uuid {:name "RUSLAN FEDOTENKO"		 :position :forward		 :number 26	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :RUSLANFEDOTENKO))
  (create-player-uuid {:name "BRUNO GERVAIS"		 :position :defender	 :number 27	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :BRUNOGERVAIS))
  (create-player-uuid {:name "CLAUDE GIROUX"		 :position :forward		 :number 28	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :CLAUDEGIROUX))
  (create-player-uuid {:name "ERIK GUSTAFSSON"		 :position :defender	 :number 29	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :ERIKGUSTAFSSON))
  (create-player-uuid {:name "ZAC RINALDO"			 :position :forward		 :number 36	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :ZACRINALDO))
  (create-player-uuid {:name "KIMMO TIMONEN"		 :position :defender	 :number 44	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :KIMMOTIMONEN))
  (create-player-uuid {:name "DANNY BRIERE"			 :position :forward		 :number 48	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :DANNYBRIERE))
  (create-player-uuid {:name "JAKUB VORACEK"		 :position :forward		 :number 93	  :teamName "Flyers" 		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :JAKUBVORACEK))
  (create-player-uuid {:name "ILYA BRYZGALOV"		 :position :goalie		 :number 30	  :teamName "Flyers"  		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :ILYABRYZGALOV))
  (create-player-uuid {:name "BRIAN BOUCHER"		 :position :goalie		 :number 33	  :teamName "Flyers"  		 :teamId (teamIdMap :NHL_PHI)}   (playerIdMap :BRIANBOUCHER))
  (create-player-uuid {:name "MATT NISKANEN"		 :position :defender	 :number 2	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :MATTNISKANEN))
  (create-player-uuid {:name "DERYK ENGELLAND"   	 :position :defender	 :number 5	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :DERYKENGELLAND))
  (create-player-uuid {:name "PAUL MARTIN"       	 :position :defender	 :number 7	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :PAULMARTIN))
  (create-player-uuid {:name "PASCAL DUPUIS"     	 :position :forward		 :number 9	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :PASCALDUPUIS))
  (create-player-uuid {:name "TANNER GLASS"      	 :position :forward		 :number 10	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :TANNERGLASS))
  (create-player-uuid {:name "CHRIS KUNITZ"  		 :position :forward		 :number 14	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :CHRISKUNITZ))
  (create-player-uuid {:name "BRANDON SUTTER"    	 :position :forward		 :number 16	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :BRANDONSUTTER))
  (create-player-uuid {:name "JAMES NEAL"        	 :position :forward		 :number 18	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :JAMESNEAL))
  (create-player-uuid {:name "BEAU BENNETT"     	 :position :forward		 :number 19	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :BEAUBENNETT))
  (create-player-uuid {:name "MATT COOKE"        	 :position :forward		 :number 24	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :MATTCOOKE))
  (create-player-uuid {:name "CRAIG ADAMS"       	 :position :forward		 :number 27	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :CRAIGADAMS))
  (create-player-uuid {:name "ROBERT BORTUZZO"   	 :position :defender	 :number 41	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :ROBERTBORTUZZO))
  (create-player-uuid {:name "BROOKS ORPIK"     	 :position :defender	 :number 44	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :BROOKSORPIK))
  (create-player-uuid {:name "JOE VITALE"       	 :position  :forward	 :number 46	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :JOEVITALE))
  (create-player-uuid {:name "TYLER KENNEDY"     	 :position :forward		 :number 48	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :TYLERKENNEDY))
  (create-player-uuid {:name "KRIS LETANG"       	 :position :defender	 :number 58	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :KRISLETANG))
  (create-player-uuid {:name "EVGENI MALKIN" 		 :position :forward		 :number 71	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :EVGENIMALKIN))
  (create-player-uuid {:name "SIDNEY CROSBY" 		 :position :forward		 :number 87	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :SIDNEYCROSBY))
  (create-player-uuid {:name "MARC-ANDRE FLEURY"	 :position :goalie		 :number 29	  :teamName "Penguins"	 	 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :MARC-ANDREFLEURY))
  (create-player-uuid {:name "TOMAS VOKOUN"  		 :position :goalie		 :number 92	  :teamName "Penguins"		 :teamId (teamIdMap :NHL_PIT)}   (playerIdMap :TOMASVOKOUN)))                                                                                                        
                                                                                                                                                 