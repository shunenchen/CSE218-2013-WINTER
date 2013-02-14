(ns hgm.db.core_test
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

;Example
;(add-gameEvents "2012-02-07-07:30PM-LA-SD" "62:00_" "A Lineup Change to : SD_F00, SD_F01, ;SD_F02 , SD_F03 , SD_G00" "San Diego")
;

;Example
;(add-gameEvents "2012-02-07-07:30PM-LA-SD" "62:00_" "A Lineup Change to : SD_F00, SD_F01, ;SD_F02 , SD_F03 , SD_G00" "San Diego")
;heroku config:add --app hockey-game-manager DYNAMODB_ACCESS_KEY=Key
;heroku config:add --app hockey-game-manager DYNAMODB_SECRET_KEY=Key