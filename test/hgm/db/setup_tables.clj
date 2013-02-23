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



Live_Game_Table
ACTIVE
game_ID
game_clock_uuid
10
5

(with-client client
	 (create-table liveGameTable
	{:read_units 10 :write_units 5}
	{:name "game_ID" :type :string}
	{:name "game_clock_uuid" :type :string})
)

(with-client client
	 (create-table pastGameTable
	{:read_units 10 :write_units 5}
	{:name "game_ID" :type :string}	)
)

(with-client client
	 (create-table playerTable
	{:read_units 10 :write_units 5}
	{:name "uuid" :type :string}
	{:name "date-time-uuid
" :type :string})
)

(with-client client
	 (create-table teamTable
	{:read_units 10 :write_units 5}
	{:name "name" :type :string}
	{:name "date-time-uuid" :type :string})
)


(with-client client
	 (create-table userTable
	{:read_units 10 :write_units 5}
	{:name "identity" :type :string})
)

(with-client client
	 (create-table playerGameStatsTable
	{:read_units 10 :write_units 5}
	{:name "player" :type :string}
	{:name "game" :type :string})
)

(with-client client
	(create-table gamePlayerStatsTable
	{:read_units 10 :write_units 5}
	{:name "game" :type :string}
	{:name "player" :type :string})
)
(with-client client
	(create-table teamGameStatsTable
	{:read_units 10 :write_units 5}
	{:name "team" :type :string}
	{:name "game" :type :string})
)
