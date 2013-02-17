(ns hgm.db.core
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.test)
  (:use hgm.db.core :reload)
  (:import com.amazonaws.auth.BasicAWSCredentials
          com.amazonaws.services.dynamodb.AmazonDynamoDBClient
          com.amazonaws.AmazonServiceException
          [com.amazonaws.services.dynamodb.model
           AttributeValue
           AttributeValueUpdate
           ComparisonOperator
           Condition
           ConditionalCheckFailedException
           CreateTableRequest
           DeleteTableRequest
           DeleteItemRequest
           DescribeTableRequest
           ExpectedAttributeValue
           GetItemRequest
           Key
           KeySchema
           KeySchemaElement
           ProvisionedThroughput
           PutItemRequest
           QueryRequest
           ScanRequest
           UpdateItemRequest]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn uuidInt []
  "Take UUID and make it a number"
 (read-string 
  (str "0x" (apply str (filter #(#{\a,\b,\c,\d,\e,\f,\1,\2,\3,\4,\5,\6,\7,\8,\9} %) (uuid))))))

(def ^{:dynamic true} *ddb_client*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;General stuff
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro doto-if
  "if cond then doto form to x,
  otherwise return x unchanged
  XXX: there has to be a better way to do this...."
  [x cond & form]
  `(if ~cond
     (doto ~x ~@form)
     ~x))

(defn create-ddb-client
  "Create the AmazonDynamoDBClient"
  [cred]
  (AmazonDynamoDBClient.
    (BasicAWSCredentials. (:access_key cred) (:secret_key cred)))
  )

(defn with-client*
  [client func]
  (binding [*ddb_client* client]
    (func)
    )
  )

(defmacro with-client
  [client & body]
  `(with-client* ~client (fn [] ~@body))
  )


;;Create an AttributeValue object from v
(defmulti create-attribute-value class)
(defmethod create-attribute-value String [v]
  {:pre [(not (empty? v))]}
  (doto (AttributeValue.) (.setS v)))

(defmethod create-attribute-value Number [v]
  {:pre [(not (empty? (str v)))]}
  (doto (AttributeValue.) (.setN (str v))))
(defmethod create-attribute-value java.util.Collection [v]
  {:pre [(not-empty v)]}
  (cond
    (every? #(and (string? %) (not (str/blank? %))) v)
    (doto (AttributeValue.) (.setSS (map str v)))

    (every? number? v)
    (doto (AttributeValue.) (.setNS (map str v)))

    :else
    (throw (Exception. "ddb sets must be all strings/numbers"))
    ))


(defn- parse-number
  "Parse a number into the correct type
  ..there has to be a better way to do this"
  [n]
  (try
    (Long/parseLong n)
    (catch NumberFormatException e
      (Double/parseDouble n))))

(defn- get-value
  "Get the value of an AttributeValue object."
  [^AttributeValue v]
  (when v
    (cond
      (.getN v) (parse-number (.getN v))
      (.getS v) (.getS v)
      (.getNS v) (set (map #(parse-number %) (.getNS v)))
      (.getSS v) (set (.getSS v)))))



(defn create-key
  "Helper to create a (clojure map) key"
  [hash_key & [range_key]]
  (-> {:hash_key hash_key}
    (#(if range_key (assoc % :range_key range_key) %))))


(defn- create-KeyObject
  "Create a com.amazonaws.services.dynamodb.model.Key object from a value."
  [hash_key & [range_key]]
    (cond
      (and hash_key range_key)
      (Key. (create-attribute-value hash_key) (create-attribute-value range_key))

      hash_key
      (Key. (create-attribute-value hash_key))
      ))

(defn KeyObject->key [k]
  (when k
    (create-key (get-value (.getHashKeyElement k)) (get-value (.getRangeKeyElement k)))))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Bonjour, World!"))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Table level functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- create-key-schema-element
  "Create a KeySchemaElement object."
  [{key_name :name key_type :type}]
  (doto (KeySchemaElement.)
    (.setAttributeName (str key_name))
    (.setAttributeType (str/upper-case (first (name key_type))))))

(defn- create-key-schema
  "hash_key - {:name \"some_name\" :type \"NUMBER\"}
  range_key - {:name \"some_name\" :type \"NUMBER\"}"
  ([hash_key]
    (doto (KeySchema. (create-key-schema-element hash_key))))

  ([hash_key range_key]
    (doto (KeySchema.)
      (.setHashKeyElement (create-key-schema-element hash_key))
      (.setRangeKeyElement (create-key-schema-element range_key))))
  )

(defn- create-provisioned-throughput
  "Created a ProvisionedThroughput object."
  [{read_units :read_units write_units :write_units}]
  (doto (ProvisionedThroughput.)
    (.setReadCapacityUnits (long read_units))
    (.setWriteCapacityUnits (long write_units))))


(defn create-table
  "Create a table in DynamoDB with the given name, throughput, and keys
  throughput - {:read_units 10 :write_units 5}"
  [name throughput & keys]
  (.createTable
    *ddb_client*
    (doto (CreateTableRequest.)
      (.setTableName (str name))
      (.setKeySchema (apply create-key-schema keys))
      (.setProvisionedThroughput (create-provisioned-throughput throughput))
      )
    )
  )

(defn delete-table
  "Delete a table in DyanmoDB with the given name."
  [name]
  (.deleteTable
    *ddb_client*
    (DeleteTableRequest. name)))

(defn describe-table
  "Describe a table in DyanmoDB with the given name."
  [name]
  (.describeTable
    *ddb_client*
    (doto (DescribeTableRequest.)
      (.setTableName name))))

(defn list-tables
  "Return a list of tables in DynamoDB."
  []
  (-> *ddb_client*
    .listTables
    .getTableNames))







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;write helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;;Expected attribute functions
(defn create-expected-attribute-value
  "Create an ExpectedAttributeValue object
  param looks like: {:exists false :value 1234}"
  [{exists :exists value :value}]
  (-> (ExpectedAttributeValue.)
    (doto-if (not (nil? exists)) (.setExists exists) )
    (doto-if value (.setValue (create-attribute-value value)) )
    )
  )

(defn prepare-expected-attribute-values
  "turns clojure-ish expected attributes into ExpectedAttributeValues
  m looks like {:someattr {:exists false :value 1234}
                :otherattr {:exists true :value 5678}}"
  [m]
  (->> m
    (map #(hash-map (name (key %)) (create-expected-attribute-value (val %))))
    (reduce merge {}))
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;put functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- prepare-map
  "Turn a clojure map into a Map<String,AttributeValue>"
  [m]
  (->> m
    (map #(hash-map (name (key %)) (create-attribute-value (val %))))
    (reduce merge {})))

(defn put-item
  "Add an item (a Clojure map) to a DynamoDB table.
  modifiers looks like {:expected {:someattr {:exists false :value 1234}
                                   :otherattr {:exists true :value 5678}}}"
  ([table item]
    (put-item table item {}))
  ([table item {expected :expected}]
    (debug "put-item: " table " item: " item " expected: " expected)
    (.putItem
      *ddb_client*
      (doto (PutItemRequest. table (prepare-map item))
        (.setExpected (prepare-expected-attribute-values expected))
        )))
  )





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;update functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;AttributeValueUpdate functions
(defn is-attribute-value-update-map? [m]
  (and (map? m)
    (or (and (contains? #{"PUT" "ADD"} (name (:action m))) (:value m))
        (and (= "DELETE" (name (get m :action)))))))


(defn create-attribute-value-update
  "create an AttributeValueUpdate object
  if v is a map, it will check :action to determine the action
  otherwise default to PUT
  "
  [v]
  (cond

    ;;If it's a DELETE with a specified value
    (and (is-attribute-value-update-map? v)
      (= "DELETE" (name (:action v)))
      (:value v))
    (doto (AttributeValueUpdate.)
      (.setValue (create-attribute-value (:value v)))
      (.setAction "DELETE"))

    ;;If it's a DELETE action with nil value
    (and (is-attribute-value-update-map? v)
         (= "DELETE" (name (:action v)))
         (nil? (:value v)))
    (doto (AttributeValueUpdate.)
      (.setAction "DELETE"))

    ;;If it's a PUT or ADD
    (is-attribute-value-update-map? v)
    (doto (AttributeValueUpdate.)
      (.setValue (create-attribute-value (:value v)))
      (.setAction (name (:action v))))

    :else
    (doto (AttributeValueUpdate.)
      (.setAction "PUT")
      (.setValue (create-attribute-value v)))
    )
  )

(defn- prepare-update-map
  "Turn a clojure map into a Map<String,AttributeValue>"
  [m]
  (->> m
    (map #(hash-map (name (key %)) (create-attribute-value-update (val %))))
    (reduce merge {})))

(defn update-item
  "Updates an item in the DB by replacing the item's specific attributes with the one's passed in"
  ([table key item]
    (update-item table key item  {}))
  ([table {hash_key :hash_key range_key :range_key} item {expected :expected}]
    (debug "update-item: " table " hash: " hash_key " range: " range_key " item: " item)
    (.updateItem
      *ddb_client*
      (doto (UpdateItemRequest.)
        (.setTableName table)
        (.setKey (create-KeyObject hash_key range_key))
        (.setAttributeUpdates (prepare-update-map item))
        (.setExpected (prepare-expected-attribute-values expected))
        ))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;delete functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn delete-item
  "Delete an item from a DynamoDB table by its hash key."
  ([table {hash_key :hash_key range_key :range_key}]
    (debug "delete-item: " table " hash: " hash_key " range: " range_key)
    (.deleteItem
      *ddb_client*
      (DeleteItemRequest. table (create-KeyObject hash_key range_key))))
  )






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;read functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn- create-condition
  "Create a Condition object
  operators can be: EQ, NE, IN, LE, LT, GE, GT,
  BETWEEN, NOT_NULL, NULL, CONTAINS, NOT_CONTAINS, BEGINS_WITH"
  [operator & attribute_values]
  (doto (Condition.)
    (.setComparisonOperator (name operator))
    (.setAttributeValueList (map create-attribute-value attribute_values)))
  )


(defn- to-map
  "Turns an dynamo item into a clojure map"
  [i]
  (if i
    (->> i
      (map #(hash-map (keyword (key %)) (get-value (val %))))
      (reduce merge {})
      (into (sorted-map)))))

(defn get-item
  "Retrieve an item from a DynamoDB table by its hash key."
  [ table {hash_key :hash_key range_key :range_key}]
  (to-map
    (.getItem
      (.getItem
        *ddb_client*
        (doto (GetItemRequest.)
          (.setTableName table)
          (.setKey (create-KeyObject hash_key range_key)))))
  )
)


(defn- create-query-request
  "Create the query request object"
  [table hash_key {range_condition :range_condition
                   range_start_key :range_start_key
                   limit :limit}]
  (-> (QueryRequest.)
    (doto
      (.setTableName table)
      (.setHashKeyValue (create-attribute-value hash_key)))
    (doto-if limit
      (.setLimit (Integer. limit)))
    (doto-if range_condition
      (.setRangeKeyCondition (apply create-condition range_condition)))
    (doto-if range_start_key
      (.setExclusiveStartKey (create-KeyObject hash_key range_start_key)))))


(def ^{:dynamic true} *query_paging_limit* 1000)
(defn query
  "Querying continues through the pages until:
  limit is hit or there are no more items
  this is to work around amazon's page limits
  query_params looks like: {:range_condition range_condition
                            :range_start_key range_start_key
                            :limit limit}"
  ([table hash_key query_params]
    (query table hash_key query_params []))
  ([table hash_key query_params items]
    (let [query_request (create-query-request table hash_key (assoc query_params :limit *query_paging_limit*))
          result (.query *ddb_client* query_request)
          limit (or (:limit query_params) (Integer/MAX_VALUE))
          last_evaluated_key (.getLastEvaluatedKey result)
          new_range_start_key (if last_evaluated_key (get-value (.getRangeKeyElement last_evaluated_key)))
          new_items (map to-map (.getItems result))
          items (into items new_items)
          ]
    ;;recurse until it either runs out of items or limit is reached
      (if (or (<= limit (count items))
            (nil? new_range_start_key))
        (vec (take limit items))
        (query table hash_key
          (assoc query_params :range_start_key new_range_start_key)
          items)
      )
  )))

(defn create-scan-filter [m]
  (if m
    (throw (Exception. "Scan filter not implemented yet"))))

(defn- prepare-attribute-list
  "Converts keywords to strings, handles nils"
  [attributes]
  (->> (or attributes [])
    (map name)
    (vec)))

(def ^{:dynamic true} *scan_paging_limit* 1000)
(defn- create-scan-request
  "Create the scan request object"
  [table {:keys [attributes_to_get exclusive_start_key]}]
  (-> (ScanRequest.)
    (.withTableName table)
    (.withAttributesToGet (prepare-attribute-list attributes_to_get))
    (.withExclusiveStartKey (create-KeyObject (:hash_key exclusive_start_key) (:range_key exclusive_start_key)))
    (.withLimit (int *scan_paging_limit*))
    ))

(defn scan
  "Do a scan request 1 or more scan requests to return full set for given params"
  [table {:keys [limit]
          :or {limit Integer/MAX_VALUE}
          :as options}]
    (loop [items []
           options options]
;      (debug items)
      (let [scan_request (create-scan-request table options)
            result (.scan *ddb_client* scan_request)
            items (into items (map to-map (.getItems result)))]

        (if (or (<= limit (count items))
                (nil? (.getLastEvaluatedKey result)))
          items
          (recur items
                (assoc options :exclusive_start_key (KeyObject->key (.getLastEvaluatedKey result))))
          )
        )))
		
;;; DB CODE GOES HERE

(def properties {:access_key (System/getenv "DYNAMODB_ACCESS_KEY") :secret_key (System/getenv "DYNAMODB_SECRET_KEY")})
(def cred (select-keys properties [:access_key :secret_key]))
(def client (create-ddb-client cred))

(def playerTable   "Player_Table")
(def teamTable     "Team_Table")
(def pastGameTable "Past_Game_Table")
(def liveGameTable "Live_Game_Table")

(defn get-forwards
  [team]
  (with-client client
	(query playerTable team {:range_condition [:BEGINS_WITH "F_"]})))

(defn get-defenders
  [team]
  (with-client client
	(query playerTable team {:range_condition [:BEGINS_WITH "D_"]})))

(defn get-goalies
  [team]
  (with-client client
	(query playerTable team {:range_condition [:BEGINS_WITH "G_"]})))

(defn get-roster
 [team]
  (with-client client
         (query playerTable team {})))

(defn get-forwards-names
    [team]
    (map :player_name (get-forwards team)))

(defn get-defenders-names
    [team]
    (map :player_name (get-defenders team)))

(defn get-goalies-names
    [team] 
    (map :player_name (get-goalies team)))

(defn get-roster-names
    [team] 
    (map :player_name (get-roster team)))

(defn game-id
  [year month day startTime awayTeam homeTeam]
  (str year \_ (format "%02d" month) \_ (format "%02d" day) \_ startTime \_
       awayTeam \@ homeTeam))

(defn live-game-exists?
  [gameId]
  (< 0 (count (with-client client (query liveGameTable gameId {:limit 1})))))

(defn game-running?
  [gameId]
  ;is there a start game event for this gameId already? - dynamo scan
  false)

(defn convert-gameClock
  [gameClock]
  (format "%07d" gameClock))

(defn add-gameEvent
  [gameId gameClock gameEvent]
  (with-client client
      (put-item liveGameTable 
	{ :game_ID gameId
	  :game_clock_uuid (str (convert-gameClock gameClock) \_ (uuid)) 
          :event (str gameEvent)})))

;(defn test-gameEvents
;  []
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 0 {:type :start})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 60000 {:type :shot :player "John Mangan"})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 75000 {:type :penalty :player "David Srour"})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 200000 {:type :shot :player "Samuel Chen"})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 220000 {:type :shot :player "John Mangan"})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 260000 {:type :penalty :player "Ben Ellis"})
;  (add-gameEvent (game-id 2012 2 7 "19:30" "SD" "LA") 3600000 {:type :end}))

(defn get-gameEvents
  ([gameId]
    (with-client client (query liveGameTable gameId {})))
  ([gameId gameClock]
    ; Will return inclusive such as a >= due to UUIDs
    (with-client client (query liveGameTable gameId
      {:range_condition [:GT (convert-gameClock gameClock)]})))
  ([gameId gameClockStart gameClockEnd]
    ; Will return inclusive of the start, exclusive of the end time
    (with-client client (query liveGameTable gameId {:range_condition
      [:BETWEEN (convert-gameClock gameClockStart)
        (convert-gameClock gameClockEnd)]}))))

(def users (atom {}))

(defn get-users
  []
  (vec (vals @users)))

(defn get-user
  [id]
  (@users id))

(defn create-user
  [m]
  ((swap! users assoc (:identity m)
          (assoc m :roles #{:official}))
   (:identity m)))

(defn update-user
  "FIXME: do something useful"
  [user roles]
  ((swap! users update-in [user] assoc :roles roles)
   user))

(defn create-game
  "FIXME: do something useful"
  [year month day startTime awayTeam homeTeam]
  ;TODO do stuff here to setup game as necessary
  (game-id year month day startTime awayTeam homeTeam))

