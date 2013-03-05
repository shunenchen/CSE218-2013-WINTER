(ns hgm.db.core
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.test)
  (:use hgm.db.clj_dynamo :reload))

(def properties {:access_key (System/getenv "DYNAMODB_ACCESS_KEY") :secret_key (System/getenv "DYNAMODB_SECRET_KEY")})
(def cred (select-keys properties [:access_key :secret_key]))
(def client (create-ddb-client cred))

(def playerTable   "Player_Table")
(def teamTable     "Team_Table")
(def teamPlayerTable "Team_Player_Table")
(def gameTable     "Game_Table")
(def gameEventTable "Game_Event_Table")
(def userTable     "User_Table")
(def playerGameStatsTable "Player_Game_Stats_Table")
(def gamePlayerStatsTable "Game_Player_Stats_Table")

;; TODO
; check to make sure we have add/update/delete for players/users/teams/games/events


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utilities
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn ^String substring?
  "True if s contains the substring."
  [substring ^String s]
  (.contains s substring))
  
(defn ^String upper-case
  "Converts string to all upper-case."
  {:added "1.2"}
  [^CharSequence s]
  (.. s toString toUpperCase))
  
(defn ^String capitalize
  "Converts first character of the string to upper-case, all other
  characters to lower-case."
  {:added "1.2"}
  [^CharSequence s]
  (let [s (.toString s)]
    (if (< (count s) 2)
      (.toUpperCase s)
      (str (.toUpperCase (subs s 0 1))
           (.toLowerCase (subs s 1))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Users
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-users
  "Returns all user objects."
  []
    (map (fn [x] (read-string (:info x))) (with-client client (scan userTable {}))))

(defn get-user
  "Returns the first user with the given google id, or nil if one doesn't exist."
  [id]
    (read-string (:info (with-client client (get-item userTable (create-key id))))))

(defn create-user
  "m is a map with an :identity key. Returns the user."
  [m]
    (let [u (assoc m :roles #{:official})]
      (with-client client
        (put-item userTable
          {:id (:identity u)
           :info (str u)}))
      u))

(defn update-user
  "m is a map with an :identity key. Returns the user."
  [id roles]
    (let [u (assoc (get-user id) :roles roles)]
      (with-client client (update-item userTable (create-key id) {:info (str u)}))
      u))

(defn delete-user
  "Deletes the user with the given id"
  [id]
    (with-client client (delete-item userTable (create-key id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Players
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-player
  "Creates a player. player is a map which must include:
   :teamId
   :position
   Returns the stored player object with an associated :id key."
  [player]
    (let [p (assoc player :id (uuid))]
      (with-client client
        (put-item playerTable
          {:id (:id p)
           :info (str p)})
        (put-item teamPlayerTable
          {:teamId (:teamId p)
           :playerId (:id p)
           :info (str p)}))
      p))

(defn update-player
  "Updates a player."
  [player]
    (let [pid (:id player)
         info (str player)
         new-tid (:teamId player)
         old-tid (:teamId (read-string (:info (with-client client
           (get-item playerTable (create-key pid))))))]
      (with-client client (update-item playerTable (create-key pid) {:info info}))
      ;Handle the teamPlayerTable corner-case of switching teams
      (if (not= new-tid old-tid)
        (with-client client
          (delete-item teamPlayerTable (create-key old-tid pid))
          (put-item teamPlayerTable {:teamId new-tid :playerId pid :info info}))
        (with-client client (update-item teamPlayerTable
          (create-key (:teamId new-tid) (:playerId pid)) {:info info})))))

;(defn delete-player
;  "Deletes a player from the data base."
;  [id]
;    (let [player (with-client client (get-item playerTable (create-key id)))]
;      (with-client client
;        (delete-item teamPlayerTable (create-key (:teamId player) id))
;        (delete-item playerTable (create-key id)))))

(defn get-players
  "Returns all player objects."
  []
    (map #(read-string (:info %)) (with-client client (scan playerTable {:attributes_to_get ["info"]}))))

(defn get-player
  "Returns the player object which should have a :id (uuid) and :teamId (team uuid)."
  [id]
    (read-string (:info (with-client client (get-item playerTable (create-key id))))))

(defn get-players-name
  "Returns all player objects with the partial name."
  [pn]	
	(filter #(substring? (upper-case pn) (:name %))
	(map #(read-string (:info %))
	(filter #(not= (:info %) " ")	
	(with-client client (scan playerTable {:attributes_to_get ["info"]}))))))
	
(defn get-roster
  "Returns the player objects associated with the given team."
  [id]
    (map #(read-string (:info %)) (with-client client (query teamPlayerTable id {}))))
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Teams
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO {delete,set}-team

(defn create-team
  "Team should be a map of team info. Returns the team object with an associated :id."
  [team]
    (let [tid (uuid)
         t (assoc team :id tid)]
      (with-client client (put-item teamTable {:id tid :data "INFO" :info (str t)}))
      (with-client client (put-item teamTable {:id tid :data "GAMES" :games (str [])}))
      t))

(defn get-teams
  "Returns all team info objects."
  []
    (map #(read-string (:info %)) (filter #(= (:data %) "INFO")
      (with-client client (scan teamTable {:attributes_to_get ["data" "info"]})))))
      
(defn get-team-info
  "Returns info via a team object."
  [id]
    (read-string (:info (with-client client (get-item teamTable (create-key id "INFO"))))))

(defn update-team-info
  "Updates the given team's info."
  [id info]
    (with-client client (update-item teamTable (create-key id "INFO") {:info (str info)})))

(defn get-team-games
  "Returns a collection of game ids for games the team has participated in."
  [id]
    (read-string (:games (with-client client (get-item teamTable (create-key id "GAMES"))))))

(defn get-team-name
  "Returns all team info objects with the partial name."
  [pn]
   (filter #(substring? (capitalize pn) (:name %))
	(map #(read-string (:info %))
	(filter #(not= (:info %) nil)	
	(with-client client (scan teamTable {:attributes_to_get ["info"]}))))))
	
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Games
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn convert-realTime
  [dateTime]
  (format "%013d" dateTime))

(defn game-id
  [startTime awayTeam homeTeam]
    (str (convert-realTime startTime) \- awayTeam \@ homeTeam))

(defn create-game
  "Input should be a map which includes the following keys:
   :startTime (timestamp)
   :homeTeam (team id)
   :awayTeam (team id)
   :status scheduled/started/ended/finalized"
  [game]
    (let [hid (:homeTeam game)
         aid (:awayTeam game)
         gid (game-id (:startTime game) aid hid)
          g (assoc game :id gid)]
      (with-client client (put-item gameTable {:id gid :info (str g)}))
      (let [aGames (read-string (:games (with-client client (get-item teamTable (create-key aid "GAMES")))))
           hGames (read-string (:games (with-client client (get-item teamTable (create-key hid "GAMES")))))]
        (with-client client
          (update-item teamTable (create-key aid "GAMES") {:games (str (conj aGames gid))})
          (update-item teamTable (create-key hid "GAMES") {:games (str (conj hGames gid))})     
     g))))

(defn get-game
  "Returns game object for the given id, or nil."
   [id]
    (try
      (read-string (:info (with-client client (get-item gameTable (create-key id)))))
      (catch Exception e nil)))

(defn get-game-ids
  "Returns ids for all created games."
   []
    (map :id (with-client client (scan gameTable {:attributes_to_get ["id"]}))))
    
(defn get-games
  "Returns all game objects."
  []
    (map #(read-string (:info %)) (with-client client (scan gameTable {:attributes_to_get ["info"]}))))

(defn update-game
  "Game should be a map with an :id key."
  [game]
    (with-client client (update-item gameTable (create-key (:id game)) {:info (str game)})))

(defn set-game-summary
  "Sets the summary of an existing game. Returns the updated game object."
  [id summary]
    (let [game (assoc (get-game id) :summary summary)]
      (update-game game)
      game))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Stats
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
(defn set-player-game-stats
  "Sets the stats for the specified player-game."
  [playerId gameId stats]
    (def entry {:playerId playerId :gameId gameId :stats (str stats)})
    (with-client client
      (put-item playerGameStatsTable entry)
      (put-item gamePlayerStatsTable entry)))

(defn get-player-game-stats
  "Gets stats for the given player over all games or an individual game."
  ([playerId]
    (map #(read-string (:stats %))
      (filter #(not= "CAREER" (:gameId %)) ;See {set,get}-player-career-stats to understand corner case
        (with-client client (query playerGameStatsTable playerId {})))))
  ([playerId gameId]
    (map #(read-string (:stats %))
      (with-client client (query playerGameStatsTable playerId
        {:range_condition [:EQ gameId]})))))

(defn get-game-player-stats
  "Gets all the stats of a game for all players or an individual player."
  ([gameId]
    (map #(read-string (:stats %))
      (filter #(not= "CAREER" (:gameId %)) ;See {set,get}-player-career-stats to understand corner case
        (with-client client (query gamePlayerStatsTable gameId {})))))
  ([gameId playerId]
    (map #(read-string (:stats %))
      (with-client client (query gamePlayerStatsTable gameId
        {:range_condition [:EQ playerId]})))))

(defn set-player-career-stats
  "Sets a player's career stats."
  [id stats]
    (set-player-game-stats id "CAREER" stats))

(defn get-player-career-stats
  "Returns a player's career stats."
  [id]
    (get-player-game-stats id "CAREER"))

(defn set-team-cumulative-stats
  "Sets a team's cumulative stats."
  [id stats]
    (with-client client (put-item teamTable (create-key id "CUMULATIVE_STATS") {:stats (str stats)})))

(defn get-team-cumulative-stats
  "Returns a team's cumulative stats over all games they've played."
  [id]
    (read-string (:stats (with-client client(get-item teamTable (create-key id "CUMULATIVE_STATS"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
(defn convert-gameClock
  [gameClock]
  (format "%07d" gameClock))

(defn add-game-event
  "Adds a new game event to the specified game at the specified clock time.
   clock should be an integer representing the game clock.
   event should be a map that has a :type key.
   Returns a string representing the gameclock with a uuid unique to the event."
  [gameId clock event]
    (let [clockUuid (str (convert-gameClock clock) \- (uuid))]
      (with-client client (put-item gameEventTable
        { :gameId gameId
          :clockUuid clockUuid
          :event (str event)}))
       clockUuid))

(defn remove-game-event
  "Removes the game event specified by the game and clockUuid."
  [gameId clockUuid]
   (with-client client (delete-item gameEventTable (create-key gameId clockUuid))))

(defn update-game-event
  "Updates a game event, optimimal if the game-clock does not change."
  ([gameId clockUuid event]
    (with-client client (update-item gameEventTable (create-key gameId clockUuid) {:event (str event)})))
  ([gameId oldClockUuid event newClock]
    (remove-game-event gameId oldClockUuid)
    (add-game-event gameId newClock event)))

(defn get-game-events
  "Returns game events for the given game, which can take 0-2 game
  clock parameters as time since the game started

  0: all events
  1: later than or equal time
  2: between clock values - inclusive of the first clock."
  ([id]
     (map #(update-in % [:event] read-string)
          (with-client client (query gameEventTable id {}))))
  ([id clock]
    ; Will return inclusive such as a >= due to UUIDs
     (map #(update-in % [:event] read-string)
          (with-client client (query gameEventTable id
            {:range_condition [:GT (convert-gameClock clock)]}))))
  ([id clockStart clockEnd]
    ; Will return inclusive of the start, exclusive of the end time
     (map #(update-in % [:event] read-string)
          (with-client client (query gameEventTable id {:range_condition
            [:BETWEEN (convert-gameClock clockStart) (convert-gameClock clockEnd)]})))))
