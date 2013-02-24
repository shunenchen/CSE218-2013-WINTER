(ns hgm.db.core
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.test)
  (:use hgm.db.clj_dynamo :reload))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def properties {:access_key (System/getenv "DYNAMODB_ACCESS_KEY") :secret_key (System/getenv "DYNAMODB_SECRET_KEY")})
(def cred (select-keys properties [:access_key :secret_key]))
(def client (create-ddb-client cred))

(def playerTable   "Player_Table")
(def teamTable     "Team_Table")
(def pastGameTable "Past_Game_Table")
(def liveGameTable "Live_Game_Table")
(def userTable     "User_Table")
(def playerGameStatsTable "Player_Game_Stats_Table")
(def gamePlayerStatsTable "Game_Player_Stats_Table")
(def teamGameStatsTable "Team_Game_Stats_Table")


(defn get-all-players
  []
    (with-client client (scan playerTable {})))

(defn get-active-players
  []
    (filter #(= "RECENT" (:date-time-uuid %)) (get-all-players)))

;; FIXME: need a way to get a single player's data by uuid
(defn get-player
  [player]
  {})

(defn get-roster
  [team]
    (filter #(= team (:team %)) (get-active-players)))

(defn get-roster-by-position
  [team position]
    (filter #(= position (:position %)) (get-roster team)))

(defn get-forwards
  [team]
    (get-roster-by-position team "forward"))

(defn get-defenders
  [team]
    (get-roster-by-position team "defender"))

(defn get-goalies
  [team]
    (get-roster-by-position team "goalie"))

;; FIXME: ignore the implementation of the next two functions, but we need "real" versions of them
(defn get-player-career-stats
  [player]
  [{:type :goal :player player :game-id "blah" :team "Los Angeles" :time 50}
   {:type :goal :player player :game-id "blah2" :team "Los Angeles" :time 60}
   {:type :on-ice :player player :game-id "blah" :time 100}])

(defn add-player-game-stats
  [playerId gameId stats]
    (def entry {:player playerId :game gameId :stats (str stats)})
    (with-client client
	  (put-item playerGameStatsTable entry)
	  (put-item gamePlayerStatsTable entry)))

(defn get-player-game-stats
  "Gets stats for the given player over all games or an individual game."
  ([playerId]
    (get-player-game-stats playerId nil))
  ([playerId gameId]
    (into [] (map (fn [x] (read-string (:stats x)))
      (with-client client (query playerGameStatsTable playerId
	    (if (nil? gameId) {} {:range_condition [:EQ gameId]})))))))

(defn get-game-player-stats
  "Gets all the stats of a game for all players or an individual player."
  ([gameId]
    (get-game-player-stats gameId nil))
  ([gameId playerId]
    (into [] (map (fn [x] (read-string (:stats x)))
      (with-client client (query gamePlayerStatsTable gameId
	    (if (nil? playerId) {} {:range_condition [:EQ playerId]})))))))

(defn add-team-game-stats
  [team gameId stats]
    (with-client client (put-item teamGameStatsTable
	  {:team team :game gameId :stats (str stats)})))
		
(defn get-team-game-stats
  "Get stats for all games the team has played in or for an individual game."
  ([team]
    (get-team-game-stats team nil))
  ([team gameId]
    (map (fn [x] (read-string (:stats x)))
	  (with-client client (query teamGameStatsTable team 
	    (if (nil? gameId) {} {:range_condition [:EQ gameId]}))))))

(defn game-id
  [year month day startTime awayTeam homeTeam]
  (str year \- (format "%02d" month) \- (format "%02d" day) \- startTime \-
       awayTeam \@ homeTeam))

(defn live-game-exists?
  [gameId]
    (< 0 (count (with-client client (query liveGameTable gameId {:limit 1})))))

(defn game-running?
  [gameId]
    (< 0 (count (filter #(= "{:type :start}" %) (map :event (with-client client (query liveGameTable gameId {})))))))

(defn convert-realTime
  [dateTime]
  (format "%013d" dateTime))

(defn convert-gameClock
  [gameClock]
  (format "%07d" gameClock))

(defn add-game-event
  [gameId gameClock gameEvent]
  (with-client client
      (put-item liveGameTable
        { :game_ID gameId
          :game_clock_uuid (str (convert-gameClock gameClock) \- (uuid))
          :event (str gameEvent)})))

;(defn test-game-events
;  []
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 0 {:type :start})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 60000 {:type :shot :player "John Mangan"})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 75000 {:type :penalty :player "David Srour"})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 200000 {:type :shot :player "Samuel Chen"})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 220000 {:type :shot :player "John Mangan"})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 260000 {:type :penalty :player "Ben Ellis"})
;  (add-game-event (game-id 2012 2 7 "19:30" "SD" "LA") 3600000 {:type :end}))

(defn get-game-events
  "Returns game events for the given game, which can take 0-2 game
  clock parameters as milliseconds since the game started

  0: all events
  1: later than or equal time
  2: between clock values - inclusive of the first clock)."
  ([gameId]
     (map #(update-in % [:event] read-string)
          (with-client client (query liveGameTable gameId {}))))
  ([gameId gameClock]
    ; Will return inclusive such as a >= due to UUIDs
     (map #(update-in % [:event] read-string)
          (with-client client (query liveGameTable gameId {:range_condition
            [:GT (convert-gameClock gameClock)]}))))
  ([gameId gameClockStart gameClockEnd]
    ; Will return inclusive of the start, exclusive of the end time
     (map #(update-in % [:event] read-string)
          (with-client client (query liveGameTable gameId {:range_condition
            [:BETWEEN (convert-gameClock gameClockStart)
             (convert-gameClock gameClockEnd)]})))))

(defn get-users
  "Returns a lazy sequence of users."
  []
    (map (fn [x] (read-string (:cmap x))) (with-client client (scan userTable {}))))

(defn get-user
  "Returns the first user with the given google id, or nil if one doesn't exist."
  [id]
    (let [user (with-client client (get-item userTable {:hash_key id}))]
      (if (nil? user) nil (read-string (:cmap user)))))

(defn create-user
  "m is a map with an :identity key. Returns the user."
  [m]
    (let [u (assoc m :roles #{:official})]
      (with-client client
        (put-item userTable
          {:identity (:identity u)
           :cmap (str u)}))
      u))

(defn update-user
  "m is a map with an :identity key. Returns the user."
  [userId roles]
    (let [u (assoc (get-user userId) :roles roles)]
      (with-client client
        (update-item userTable {:hash_key userId} {:cmap (str u)}))
      u))

(defn delete-user
  "Deletes the user with the given id"
  [id]
    (with-client client (delete-item userTable {:hash_key id}))
    nil)

(defn create-game
  "FIXME: do something useful"
  [year month day startTime awayTeam homeTeam]
  ;TODO do stuff here to setup game as necessary
  (game-id year month day startTime awayTeam homeTeam))

(defn get-player-events
  "FIXME: get all events for the given player"
  [player]
  {})

