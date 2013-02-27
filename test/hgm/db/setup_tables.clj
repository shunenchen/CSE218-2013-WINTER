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

;(with-client client
;	 (delete-table	playerTable)
;	 (delete-table	teamPlayerTable)
;	 (delete-table	gameTable)
;	 (delete-table	liveGameTable)
;	 (delete-table	userTable)
;	 (delete-table	playerGameStatsTable)
;	 (delete-table	gamePlayerStatsTable)
;	 (delete-table	teamGameStatsTable))


(with-client client
	(create-table liveGameTable
	{:read_units 10 :write_units 5}
	{:name "id" :type :string}
	{:name "clockUuid" :type :string}))

(with-client client
	 (create-table gameTable
	{:read_units 10 :write_units 5}
	{:name "status" :type :string}
	{:name "id" :type :string}))

(with-client client
	(create-table playerTable
	{:read_units 10 :write_units 5}
	{:name "id" :type :string})))
	
(with-client client
	(create-table teamPlayerTable
	{:read_units 10 :write_units 5}
	{:name "teamId" :type :string}
	{:name "playerId" :type :string}))
	
(with-client client
	 (create-table teamTable
	{:read_units 10 :write_units 5}
	{:name "id" :type :string}
	{:name "data" :type :string}))

(with-client client
	 (create-table userTable
	{:read_units 10 :write_units 5}
	{:name "id" :type :string}))

(with-client client
	 (create-table playerGameStatsTable
	{:read_units 10 :write_units 5}
	{:name "playerId" :type :string}
	{:name "gameId" :type :string}))

(with-client client
	(create-table gamePlayerStatsTable
	{:read_units 10 :write_units 5}
	{:name "gameId" :type :string}
	{:name "playerId" :type :string}))
	
(with-client client
	(create-table teamGameStatsTable
	{:read_units 10 :write_units 5}
	{:name "teamId" :type :string}
	{:name "gameId" :type :string}))
