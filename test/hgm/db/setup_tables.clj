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

(def playerTable   "Player_Table")
(def teamTable     "Team_Table")
(def pastGameTable "Past_Game_Table")
(def liveGameTable "Live_Game_Table")

(def players [
{:UUID (uuid) :name "LA_F00" :team "Los Angeles" :jersey_number "00" :position "Forward"}
{:UUID (uuid) :name "LA_F01" :team "Los Angeles" :jersey_number "01" :position "Forward"} 
{:UUID (uuid) :name "LA_F02" :team "Los Angeles" :jersey_number "02" :position "Forward"}
{:UUID (uuid) :name "LA_F03" :team "Los Angeles" :jersey_number "03" :position "Forward"}
{:UUID (uuid) :name "LA_F04" :team "Los Angeles" :jersey_number "04" :position "Forward"}
{:UUID (uuid) :name "LA_F05" :team "Los Angeles" :jersey_number "05" :position "Forward"}
{:UUID (uuid) :name "LA_F06" :team "Los Angeles" :jersey_number "06" :position "Forward"}
{:UUID (uuid) :name "LA_F07" :team "Los Angeles" :jersey_number "07" :position "Forward"}
{:UUID (uuid) :name "LA_F08" :team "Los Angeles" :jersey_number "08" :position "Forward"}
{:UUID (uuid) :name "LA_F09" :team "Los Angeles" :jersey_number "09" :position "Forward"}
{:UUID (uuid) :name "LA_F10" :team "Los Angeles" :jersey_number "10" :position "Forward"}
{:UUID (uuid) :name "LA_F11" :team "Los Angeles" :jersey_number "11" :position "Forward"}
{:UUID (uuid) :name "LA_D00" :team "Los Angeles" :jersey_number "12" :position "Defense"}
{:UUID (uuid) :name "LA_D01" :team "Los Angeles" :jersey_number "13" :position "Defense"}
{:UUID (uuid) :name "LA_D02" :team "Los Angeles" :jersey_number "14" :position "Defense"}
{:UUID (uuid) :name "LA_D03" :team "Los Angeles" :jersey_number "15" :position "Defense"}
{:UUID (uuid) :name "LA_D04" :team "Los Angeles" :jersey_number "16" :position "Defense"}
{:UUID (uuid) :name "LA_D05" :team "Los Angeles" :jersey_number "17" :position "Defense"}
{:UUID (uuid) :name "LA_G00" :team "Los Angeles" :jersey_number "18" :position "Goalie" }
{:UUID (uuid) :name "LA_G01" :team "Los Angeles" :jersey_number "19" :position "Goalie" }
{:UUID (uuid) :name "SD_F00" :team "San Diego"   :jersey_number "00" :position "Forward"}
{:UUID (uuid) :name "SD_F01" :team "San Diego"   :jersey_number "01" :position "Forward"} 
{:UUID (uuid) :name "SD_F02" :team "San Diego"   :jersey_number "02" :position "Forward"}
{:UUID (uuid) :name "SD_F03" :team "San Diego"   :jersey_number "03" :position "Forward"}
{:UUID (uuid) :name "SD_F04" :team "San Diego"   :jersey_number "04" :position "Forward"}
{:UUID (uuid) :name "SD_F05" :team "San Diego"   :jersey_number "05" :position "Forward"}
{:UUID (uuid) :name "SD_F06" :team "San Diego"   :jersey_number "06" :position "Forward"}
{:UUID (uuid) :name "SD_F07" :team "San Diego"   :jersey_number "07" :position "Forward"}
{:UUID (uuid) :name "SD_F08" :team "San Diego"   :jersey_number "08" :position "Forward"}
{:UUID (uuid) :name "SD_F09" :team "San Diego"   :jersey_number "09" :position "Forward"}
{:UUID (uuid) :name "SD_F10" :team "San Diego"   :jersey_number "10" :position "Forward"}
{:UUID (uuid) :name "SD_F11" :team "San Diego"   :jersey_number "11" :position "Forward"}
{:UUID (uuid) :name "SD_D00" :team "San Diego"   :jersey_number "12" :position "Defense"}
{:UUID (uuid) :name "SD_D01" :team "San Diego"   :jersey_number "13" :position "Defense"}
{:UUID (uuid) :name "SD_D02" :team "San Diego"   :jersey_number "14" :position "Defense"}
{:UUID (uuid) :name "SD_D03" :team "San Diego"   :jersey_number "15" :position "Defense"}
{:UUID (uuid) :name "SD_D04" :team "San Diego"   :jersey_number "16" :position "Defense"}
{:UUID (uuid) :name "SD_D05" :team "San Diego"   :jersey_number "17" :position "Defense"}
{:UUID (uuid) :name "SD_G00" :team "San Diego"   :jersey_number "18" :position "Goalie" }
{:UUID (uuid) :name "SD_G01" :team "San Diego"   :jersey_number "19" :position "Goalie" }]
)

(def teams [
{:Team_Name "Los Angeles" :location "Los Angeles,CA" :roster "TBA"}
{:Team_Name "San Diego"   :location "San Diego,CA" :roster "TBA2"}]
)

(def pastGame [
{:Game_ID "2012-02-03-07:30PM-LA-SD" :events "TBD"}
{:Game_ID "2012-02-05-07:30PM-LA-SD" :events "TBD2"}]
)

(def liveGame [
{:Game_Event_ID (uuid) :Game_ID "2012-02-07-07:30PM-LA-SD" 
 :event "Game_Start" :game_clock "20:00" }
{:Game_Event_ID (uuid) :Game_ID "2012-02-07-07:30PM-LA-SD" :team "Los Angeles"
 :event "Lineup Change to : LA_F00, LA_F01, LA_F02 , LA_F03 , LA_G00 " :game_clock "20:00" }
{:Game_Event_ID (uuid) :Game_ID "2012-02-07-07:30PM-LA-SD" :team "San Diego"
 :event "Lineup Change to : SD_F00, SD_F01, SD_F02 , SD_F03 , SD_G00 " :game_clock "20:00" }
 ]
)

(with-client client
  (create-table
	playerTable
    {:read_units 5 :write_units 10}
	{:name "UUID" :type :string}
  )
)

(with-client client
  (create-table
	teamTable
    {:read_units 5 :write_units 10}
	{:name "Team_Name" :type :string}
  )
)

(with-client client
  (create-table
	pastGameTable
    {:read_units 5 :write_units 10}
	{:name "Game_ID" :type :string}
  )
)

(with-client client
  (create-table
	liveGameTable
    {:read_units 5 :write_units 10}
	{:name "Game_Event_ID" :type :string}
  )
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