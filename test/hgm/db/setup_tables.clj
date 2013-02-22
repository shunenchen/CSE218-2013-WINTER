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

;(try
;  (with-client client
;   (delete-table	playerTable)
;   (delete-table	teamTable)
;   (delete-table	pastGameTable)
;   (delete-table	liveGameTable)
;  ) (catch Exception e ())
;)


(with-client client
   (create-table
	playerTable
    {:read_units 2 :write_units 2}
	{:name "team" :type :string}
	{:name "position_number" :type :string}
  ))
  
(with-client client
    (create-table
	  teamTable
      {:read_units 2 :write_units 2}
	  {:name "team" :type :string}
	  {:name "roster" :type :string}
    ))
	
(with-client client
  (create-table
	pastGameTable
    {:read_units 2 :write_units 2}
	{:name "game_ID" :type :string}
    ))
	
(with-client client
  (create-table
	liveGameTable
    {:read_units 2 :write_units 2}
	{:name "game_ID" :type :string}
	{:name "game_clock_game_event_ID" :type :string}
    ))

