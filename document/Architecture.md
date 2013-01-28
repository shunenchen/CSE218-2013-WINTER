Hockey Game Manager 
22 January 2013

Team: Samuel (Shun-En) Chen, Ben Ellis, Cameron Helm, John Mangan, Eric Seidel, David Srour

Overview: The Hockey Game Manager is a tool to assist in live tracking of hockey game statistics, as well as the viewing of all such tracked information.

User Stories:
As an administrator I...

* shall be able to mark (or unmark) any viewer as an editor.
* shall be able to mark any viewer as an administrator.
* shall be able to delete non-administrators.

As an editor I...
* shall be able to change the roster, the team, the players.
* shall be able to create or delete a game.
* shall be able to enter events during the game including but not limited to players entering and exiting the ice, scoring/assists, and penalties.
* shall be able to start and stop a simulated game clock.
* shall be able to create/modify/delete events during or after the game.
* shall be able to mark flagged events as reviewed (and thus unflaggable).

As an authenticated viewer I...
* shall be able to flag events for review.

As a viewer I...
* shall be able to view any stored events.
* shall be able to view a game.
* shall be able to view a single game¡¦s events, a team¡¦s statistics/roster, and a player¡¦s statistics.
* shall be able to search games, teams and players.
* shall be able to view game summary of events.
* shall be able to register as an authenticated viewer.

Other Requirements:
* Users will have a web interface for all desired interactions.

All events shall be timestamped by the game clock.

* Data entered/edited locally should be stored remotely pending any verification delay.
* All data shall be stored in the cloud in a manner to assure redundancy and security.
* All data shall be transmitted using a security protocol (such as SSL).
* The system shall be able to handle simultaneous games.
* All writes to the back-end shall be authenticated.

Logical View
	
![[Entity-Relationship.png|alt=Entity Relationship Diagram]]

Event types
	Score / Save / Shot / Assist
	Penalty
	Injury
	Enter/exit ice time
	Fight
	(Subject to addition)

Hierarchical Modules
![[Hierarchy.png|alt=Hierarchy]]

Implementation View
Language
		Clojure
Library
	Ring
	compojure
	hiccup

Deployment View
Heroku ( Host + deployment )
	Slug -- Scalability
AWS	(Database)
	SQL vs NoSQL 

10,000 Foot View 
![[1000FootView.png|alt=10,000 foot view]]


Process View

Security
		Communication will be over HTTPS. 
Storage secured by AWS
Users accounts will have permission and passwords.
Passwords will be encrypted.
QoS
		To be defined.
Scalability
		Heroku scales the application
		AWS scales the database