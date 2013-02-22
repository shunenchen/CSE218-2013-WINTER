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

(defn create-player
  [dateTime name position number team]
  (let [pUuid (uuid)]
    [{:uuid pUuid :date-time-uuid (str (convert-realTime dateTime) \- (uuid)) 
 	  :name name :position position :number (format "%02d" number) :team team}
	 {:uuid pUuid :date-time-uuid "RECENT"
 	  :name name :position position :number (format "%02d" number) :team team}]))

(def players (into [] (concat
  (create-player 19420 "BRAYDON COBURN"	"defender"	5	"Flyers" )
  (create-player 19420 "NICKLAS GROSSMANN"	"defender"	8	"Flyers" ) 
  (create-player 19420 "MIKE KNUBLE"		"forward"	9	"Flyers" )
  (create-player 19420 "BRAYDEN SCHENN"	"forward"	10	"Flyers" )
  (create-player 19420 "HARRY ZOLNIERCZYK"	"forward"	12	"Flyers" )
  (create-player 19420 "SEAN COUTURIER"	"forward"	14	"Flyers" )
  (create-player 19420 "WAYNE SIMMONDS"	"forward"	17	"Flyers" )
  (create-player 19420 "LUKE SCHENN"		"defender"	22	"Flyers" )
  (create-player 19420 "MATT READ"			"forward"	24	"Flyers" )
  (create-player 19420 "MAXIME TALBOT"		"forward"	25	"Flyers" )
  (create-player 19420 "RUSLAN FEDOTENKO"	"forward"	26	"Flyers" )
  (create-player 19420 "BRUNO GERVAIS"		"defender"	27	"Flyers" )
  (create-player 19420 "CLAUDE GIROUX (C)"	"forward"	28	"Flyers" )
  (create-player 19420 "ERIK GUSTAFSSON"	"defender"	29	"Flyers" )
  (create-player 19420 "ZAC RINALDO"		"forward"	36	"Flyers" )
  (create-player 19420 "KIMMO TIMONEN (A)"	"defender"	44	"Flyers" )
  (create-player 19420 "DANNY BRIERE (A)"	"forward"	48	"Flyers" )
  (create-player 19420 "JAKUB VORACEK"		"forward"	93	"Flyers" )
  (create-player 19420 "ILYA BRYZGALOV"	"goalie"		30	"Flyers" )
  (create-player 19420 "BRIAN BOUCHER"		"goalie"		33	"Flyers" )
  (create-player 19420 "MATT NISKANEN"		"defender"	2	"Penguins")
  (create-player 19420 "DERYK ENGELLAND"   	"defender"	5	"Penguins")
  (create-player 19420 "PAUL MARTIN"       "defender"	7	"Penguins")
  (create-player 19420 "PASCAL DUPUIS"     "forward"	9	"Penguins")
  (create-player 19420 "TANNER GLASS"      "forward"	10	"Penguins")
  (create-player 19420 "CHRIS KUNITZ (A)"  	"forward"	14	"Penguins")
  (create-player 19420 "BRANDON SUTTER"    "forward"	16	"Penguins")
  (create-player 19420 "JAMES NEAL"        "forward"	18	"Penguins")
  (create-player 19420 "BEAU BENNETT"     	"forward"	19	"Penguins")
  (create-player 19420 "MATT COOKE"        "forward"	24	"Penguins")
  (create-player 19420 "CRAIG ADAMS"       "forward"	27	"Penguins")
  (create-player 19420 "ROBERT BORTUZZO"   	"defender"	41	"Penguins")
  (create-player 19420 "BROOKS ORPIK"     	"defender"	44	"Penguins")
  (create-player 19420 "JOE VITALE"        "forward"	46	"Penguins")
  (create-player 19420 "TYLER KENNEDY"     "forward"	48	"Penguins")
  (create-player 19420 "KRIS LETANG"       "defender"	58	"Penguins")
  (create-player 19420 "EVGENI MALKIN (A)" 	"forward"	71	"Penguins")
  (create-player 19420 "SIDNEY CROSBY (C)" 	"forward"	87	"Penguins")
  (create-player 19420 "MARC-ANDRE FLEURY"	"goalie"		29	"Penguins")
  (create-player 19420 "TOMAS VOKOUN"  	"goalie"		92	"Penguins"))))
(defn add-all-players 
  []
  (with-client client
    (doseq [i (range (count players))]
    (put-item playerTable (players i)))))
  