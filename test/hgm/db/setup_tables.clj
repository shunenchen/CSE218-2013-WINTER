(ns hgm.db.setup-tables
  (:use clojure.test)
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.instant)
  (:use hgm.db.core :reload)
  (:import com.amazonaws.auth.BasicAWSCredentials
           com.amazonaws.services.dynamodb.AmazonDynamoDBClient
           com.amazonaws.AmazonServiceException
           [com.amazonaws.services.dynamodb.model
            ConditionalCheckFailedException
            ])
)

(def properties (-> (clojure.java.io/resource "aws.properties.clj")
                    (clojure.java.io/reader)
                    (java.io.PushbackReader.)
                    (read)))

  (def cred (select-keys properties [:access_key :secret_key]))

  (def test_table (:test_table properties))
  (def client (create-ddb-client cred))

(def players [
{:team "Los Angeles" :position_number "F_00" :player_name "LA_F00" }
{:team "Los Angeles" :position_number "F_01" :player_name "LA_F01" } 
{:team "Los Angeles" :position_number "F_02" :player_name "LA_F02" }
{:team "Los Angeles" :position_number "F_03" :player_name "LA_F03" }
{:team "Los Angeles" :position_number "F_04" :player_name "LA_F04" }
{:team "Los Angeles" :position_number "F_05" :player_name "LA_F05" }
{:team "Los Angeles" :position_number "F_06" :player_name "LA_F06" }
{:team "Los Angeles" :position_number "F_07" :player_name "LA_F07" }
{:team "Los Angeles" :position_number "F_08" :player_name "LA_F08" }
{:team "Los Angeles" :position_number "F_09" :player_name "LA_F09" }  
{:team "Los Angeles" :position_number "F_10" :player_name "LA_F10" }  
{:team "Los Angeles" :position_number "F_11" :player_name "LA_F11" }   
{:team "Los Angeles" :position_number "D_12" :player_name "LA_D00" }   
{:team "Los Angeles" :position_number "D_13" :player_name "LA_D01" }  
{:team "Los Angeles" :position_number "D_14" :player_name "LA_D02" }   
{:team "Los Angeles" :position_number "D_15" :player_name "LA_D03" }   
{:team "Los Angeles" :position_number "D_16" :player_name "LA_D04" }   
{:team "Los Angeles" :position_number "D_17" :player_name "LA_D05" }   
{:team "Los Angeles" :position_number "G_18" :player_name "LA_G00" }   
{:team "Los Angeles" :position_number "G_19" :player_name "LA_G01" } 
{:team "San Diego"   :position_number "F_00" :player_name "SD_F00" }
{:team "San Diego"   :position_number "F_01" :player_name "SD_F01" } 
{:team "San Diego"   :position_number "F_02" :player_name "SD_F02" }
{:team "San Diego"   :position_number "F_03" :player_name "SD_F03" }
{:team "San Diego"   :position_number "F_04" :player_name "SD_F04" }
{:team "San Diego"   :position_number "F_05" :player_name "SD_F05" }
{:team "San Diego"   :position_number "F_06" :player_name "SD_F06" }
{:team "San Diego"   :position_number "F_07" :player_name "SD_F07" }
{:team "San Diego"   :position_number "F_08" :player_name "SD_F08" }
{:team "San Diego"   :position_number "F_09" :player_name "SD_F09" }  
{:team "San Diego"   :position_number "F_10" :player_name "SD_F10" }  
{:team "San Diego"   :position_number "F_11" :player_name "SD_F11" }   
{:team "San Diego"   :position_number "D_12" :player_name "SD_D00" }   
{:team "San Diego"   :position_number "D_13" :player_name "SD_D01" }  
{:team "San Diego"   :position_number "D_14" :player_name "SD_D02" }   
{:team "San Diego"   :position_number "D_15" :player_name "SD_D03" }   
{:team "San Diego"   :position_number "D_16" :player_name "SD_D04" }   
{:team "San Diego"   :position_number "D_17" :player_name "SD_D05" }   
{:team "San Diego"   :position_number "G_18" :player_name "SD_G00" }   
{:team "San Diego"   :position_number "G_19" :player_name "SD_G01" }]
)

(def teams [
{:team "Los Angeles" :roster "TBA"  :location "Los Angeles,CA" }
{:team "San Diego"   :roster "TBA2" :location "San Diego,CA" }]
)

(def pastGame [
{:game_ID "2012-02-03-07:30PM-LA-SD" :events "TBD"}
{:game_ID "2012-02-05-07:30PM-LA-SD" :events "TBD2"}]
)

(def liveGame [
{:game_ID "2012-02-07-07:30PM-LA-SD" :game_clock_game_event_ID (str "60:00_" (uuid)) 
 :event "Game_Start"}
{:game_ID "2012-02-07-07:30PM-LA-SD" :game_clock_game_event_ID (str "60:00_" (uuid)) 
 :event "Lineup Change to : LA_F00, LA_F01, LA_F02 , LA_F03 , LA_G00" :team "Los Angeles" }
{:game_ID "2012-02-07-07:30PM-LA-SD" :game_clock_game_event_ID (str "60:00_" (uuid)) 
 :event "Lineup Change to : SD_F00, SD_F01, SD_F02 , SD_F03 , SD_G00" :team "San Diego" } ]
)

(try
  (with-client client
   (delete-table	playerTable)
   (delete-table	teamTable)
   (delete-table	pastGameTable)
   (delete-table	liveGameTable)
  ) (catch Exception e ())
)

(try
  (with-client client
   (create-table
	playerTable
    {:read_units 2 :write_units 2}
	{:name "team" :type :string}
	{:name "position_number" :type :string}
  )) (catch Exception e ())
  )
  
(try
  (with-client client
    (create-table
	  teamTable
      {:read_units 2 :write_units 2}
	  {:name "team" :type :string}
	  {:name "roster" :type :string}
    ))(catch Exception e ())
)
 
(try
  (with-client client
  (create-table
	pastGameTable
    {:read_units 2 :write_units 2}
	{:name "game_ID" :type :string}
    ))(catch Exception e ())
)

(try
  (with-client client
  (create-table
	liveGameTable
    {:read_units 2 :write_units 2}
	{:name "game_ID" :type :string}
	{:name "game_clock_game_event_ID" :type :string}
    ))(catch Exception e ())
)

(with-client client
      (doseq [i (range 40)]
      (put-item playerTable (players i))))

(with-client client
      (doseq [i (range 2)]
      (put-item teamTable (teams i))))

(with-client client
      (doseq [i (range 2)]
      (put-item pastGameTable (pastGame i))))

(with-client client
      (doseq [i (range 3)]
      (put-item liveGameTable (liveGame i))))

;(defn get-forward
;  [team]
;  (with-client client
;	(query playerTable team {:range_condition [:BEGINS_WITH "F_"]}))
;)
;
;(defn get-defense
;  [team]
;  (with-client client
;	(query playerTable team {:range_condition [:BEGINS_WITH "D_"]}))
;)
;
;(defn get-goalie
;  [team]
;  (with-client client
;	(query playerTable team {:range_condition [:BEGINS_WITH "G_"]}))
;)
;
;(defn add-gameEvents
;  [gameID gameClock gameEvent teamInovled]
;  (with-client client
;      (put-item liveGameTable 
;	  {:game_ID gameID :game_clock_game_event_ID (str gameClock (uuid)) 
;		:event gameEvent  :team teamInovled }
;		)
;	)
;)
;;Example
;;(add-gameEvents "2012-02-07-07:30PM-LA-SD" "62:00_" "A Lineup Change to : SD_F00, SD_F01, ;SD_F02 , SD_F03 , SD_G00" "San Diego")
;