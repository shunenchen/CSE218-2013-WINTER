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

(def playerUUIDMap {:BRAYDONCOBURN (uuid) :NICKLASGROSSMANN (uuid) :MIKEKNUBLE (uuid) :BRAYDENSCHENN (uuid) :HARRYZOLNIERCZYK (uuid) :SEANCOUTURIER (uuid) :WAYNESIMMONDS (uuid) :LUKESCHENN (uuid) :MATTREAD (uuid) :MAXIMETALBOT (uuid) :RUSLANFEDOTENKO (uuid) :BRUNOGERVAIS (uuid) :CLAUDEGIROUX (uuid) :ERIKGUSTAFSSON (uuid) :ZACRINALDO (uuid) :KIMMOTIMONEN (uuid) :DANNYBRIERE (uuid) :JAKUBVORACEK (uuid) :ILYABRYZGALOV (uuid) :BRIANBOUCHER (uuid) :MATTNISKANEN (uuid) :DERYKENGELLAND (uuid) :PAULMARTIN (uuid) :PASCALDUPUIS (uuid) :TANNERGLASS (uuid) :CHRISKUNITZ (uuid) :BRANDONSUTTER (uuid) :JAMESNEAL (uuid) :BEAUBENNETT (uuid) :MATTCOOKE (uuid) :CRAIGADAMS (uuid) :ROBERTBORTUZZO (uuid) :BROOKSORPIK (uuid) :JOEVITALE (uuid) :TYLERKENNEDY (uuid) :KRISLETANG (uuid) :EVGENIMALKIN (uuid) :SIDNEYCROSBY (uuid) :MARC-ANDREFLEURY (uuid) :TOMASVOKOUN (uuid) })

(defn create-player-uuid
  [dateTime name position number team pUuid]
     [{:uuid pUuid :date-time-uuid (str (convert-realTime dateTime) \- (uuid)) 
 	  :name name :position position :number (format "%02d" number) :team team}
	 {:uuid pUuid :date-time-uuid "RECENT"
 	  :name name :position position :number (format "%02d" number) :team team}])

(def players (into [] (concat
  [{:uuid "0" :date-time-uuid "0" :uuidHashMap (str playerUUIDMap)}]
  (create-player-uuid 19420 "BRAYDON COBURN"	"defender"	5	"Flyers" 	(playerUUIDMap :BRAYDONCOBURN))
  (create-player-uuid 19420 "NICKLAS GROSSMANN"	"defender"	8	"Flyers" 	(playerUUIDMap :NICKLASGROSSMANN)) 
  (create-player-uuid 19420 "MIKE KNUBLE"		"forward"	9	"Flyers" 	(playerUUIDMap :MIKEKNUBLE))
  (create-player-uuid 19420 "BRAYDEN SCHENN"	"forward"	10	"Flyers" 	(playerUUIDMap :BRAYDENSCHENN))
  (create-player-uuid 19420 "HARRY ZOLNIERCZYK"	"forward"	12	"Flyers" 	(playerUUIDMap :HARRYZOLNIERCZYK))
  (create-player-uuid 19420 "SEAN COUTURIER"	"forward"	14	"Flyers" 	(playerUUIDMap :SEANCOUTURIER))
  (create-player-uuid 19420 "WAYNE SIMMONDS"	"forward"	17	"Flyers" 	(playerUUIDMap :WAYNESIMMONDS))
  (create-player-uuid 19420 "LUKE SCHENN"		"defender"	22	"Flyers" 	(playerUUIDMap :LUKESCHENN))
  (create-player-uuid 19420 "MATT READ"		"forward"	24	"Flyers" 	(playerUUIDMap :MATTREAD))
  (create-player-uuid 19420 "MAXIME TALBOT"		"forward"	25	"Flyers" 	(playerUUIDMap :MAXIMETALBOT))
  (create-player-uuid 19420 "RUSLAN FEDOTENKO"	"forward"	26	"Flyers" 	(playerUUIDMap :RUSLANFEDOTENKO))
  (create-player-uuid 19420 "BRUNO GERVAIS"		"defender"	27	"Flyers" 	(playerUUIDMap :BRUNOGERVAIS))
  (create-player-uuid 19420 "CLAUDE GIROUX"		"forward"	28	"Flyers" 	(playerUUIDMap :CLAUDEGIROUX))
  (create-player-uuid 19420 "ERIK GUSTAFSSON"	"defender"	29	"Flyers" 	(playerUUIDMap :ERIKGUSTAFSSON))
  (create-player-uuid 19420 "ZAC RINALDO"		"forward"	36	"Flyers" 	(playerUUIDMap :ZACRINALDO))
  (create-player-uuid 19420 "KIMMO TIMONEN"		"defender"	44	"Flyers" 	(playerUUIDMap :KIMMOTIMONEN))
  (create-player-uuid 19420 "DANNY BRIERE"		"forward"	48	"Flyers" 	(playerUUIDMap :DANNYBRIERE))
  (create-player-uuid 19420 "JAKUB VORACEK"		"forward"	93	"Flyers" 	(playerUUIDMap :JAKUBVORACEK))
  (create-player-uuid 19420 "ILYA BRYZGALOV"	"goalie"		30	"Flyers" 	(playerUUIDMap :ILYABRYZGALOV))
  (create-player-uuid 19420 "BRIAN BOUCHER"		"goalie"		33	"Flyers" 	(playerUUIDMap :BRIANBOUCHER))
  (create-player-uuid 19420 "MATT NISKANEN"		"defender"	2	"Penguins"	(playerUUIDMap :MATTNISKANEN))
  (create-player-uuid 19420 "DERYK ENGELLAND"   "defender"	5	"Penguins"	(playerUUIDMap :DERYKENGELLAND))
  (create-player-uuid 19420 "PAUL MARTIN"       "defender"	7	"Penguins"	(playerUUIDMap :PAULMARTIN))
  (create-player-uuid 19420 "PASCAL DUPUIS"     "forward"	9	"Penguins"	(playerUUIDMap :PASCALDUPUIS))
  (create-player-uuid 19420 "TANNER GLASS"      "forward"	10	"Penguins"	(playerUUIDMap :TANNERGLASS))
  (create-player-uuid 19420 "CHRIS KUNITZ"  	"forward"	14	"Penguins"	(playerUUIDMap :CHRISKUNITZ))
  (create-player-uuid 19420 "BRANDON SUTTER"    "forward"	16	"Penguins"	(playerUUIDMap :BRANDONSUTTER))
  (create-player-uuid 19420 "JAMES NEAL"        "forward"	18	"Penguins"	(playerUUIDMap :JAMESNEAL))
  (create-player-uuid 19420 "BEAU BENNETT"     	"forward"	19	"Penguins"	(playerUUIDMap :BEAUBENNETT))
  (create-player-uuid 19420 "MATT COOKE"        "forward"	24	"Penguins"	(playerUUIDMap :MATTCOOKE))
  (create-player-uuid 19420 "CRAIG ADAMS"       "forward"	27	"Penguins"	(playerUUIDMap :CRAIGADAMS))
  (create-player-uuid 19420 "ROBERT BORTUZZO"   "defender"	41	"Penguins"	(playerUUIDMap :ROBERTBORTUZZO))
  (create-player-uuid 19420 "BROOKS ORPIK"     	"defender"	44	"Penguins"	(playerUUIDMap :BROOKSORPIK))
  (create-player-uuid 19420 "JOE VITALE"        "forward"	46	"Penguins"	(playerUUIDMap :JOEVITALE))
  (create-player-uuid 19420 "TYLER KENNEDY"     "forward"	48	"Penguins"	(playerUUIDMap :TYLERKENNEDY))
  (create-player-uuid 19420 "KRIS LETANG"       "defender"	58	"Penguins"	(playerUUIDMap :KRISLETANG))
  (create-player-uuid 19420 "EVGENI MALKIN" 	"forward"	71	"Penguins"	(playerUUIDMap :EVGENIMALKIN))
  (create-player-uuid 19420 "SIDNEY CROSBY" 	"forward"	87	"Penguins"	(playerUUIDMap :SIDNEYCROSBY))
  (create-player-uuid 19420 "MARC-ANDRE FLEURY"	"goalie"		29	"Penguins"	(playerUUIDMap :MARC-ANDREFLEURY))
  (create-player-uuid 19420 "TOMAS VOKOUN"  	"goalie"		92	"Penguins"	(playerUUIDMap :TOMASVOKOUN)))))
  
(defn add-all-players 
  []
  (with-client client
    (doseq [i (range (count players))]
    (put-item playerTable (players i)))))
  