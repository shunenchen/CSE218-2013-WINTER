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

;heroku config:add --app hockey-game-manager DYNAMODB_ACCESS_KEY=Key
;heroku config:add --app hockey-game-manager DYNAMODB_SECRET_KEY=Key