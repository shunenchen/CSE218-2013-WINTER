//Compressed Queue Functions
function Queue(){
        var _1=[];
        var _2=0;
        this.getLength=function(){	//length
                return (_1.length-_2);
        };
        this.isEmpty=function(){	//returns true if empty
                return (_1.length==0);
        };
        this.enqueue=function(_3){	//add to queue
                _1.push(_3);
        };
        this.dequeue=function(){	//return element removed from queue
                if(_1.length==0){
                        return undefined;
                }
                var _4=_1[_2];
                if(++_2*2>=_1.length){
                        _1=_1.slice(_2);
                        _2=0;
                }
                return _4;
        };
        this.peek=function(){		//return the element at head of queue
                return (_1.length>0?_1[_2]:undefined);
        };
};

var GAME_ID = "0001366936200-4532f8ad-e638-43a3-8de7-b8d4f4b7845b@46b24778-e521-4c76-b714-1c61450242ec";

var HOME = { id: "46b24778-e521-4c76-b714-1c61450242ec" };
var AWAY = { id: "4532f8ad-e638-43a3-8de7-b8d4f4b7845b" };

//Game Timer Functions
function StartTime(id,srt){
    $.post("/events/start-game", {
        gameId: GAME_ID,
        startTime: Date.now(),
        home: { teamId: HOME.id,
                // these should not be the same!
                roster: HOME.players,
                starting: HOME.players
              },
        away: { teamId: AWAY.id,
                // these should not be the same!
                roster: AWAY.players,
                starting: AWAY.players
              }
    }// , function (data) {
     //    GAME_ID = data.gameId;
     //    console.log('GAME_ID: ' + GAME_ID);
    );
    var obj=document.getElementById(id),ms=obj.value.split(/\W/);
    if (StartTime[id]){
        clearTimeout(StartTime[id].to);
    }
    if (srt && isFinite(ms[0]) && isFinite(ms[1])){
        StartTime[id]={
            obj:obj,
                srt:new Date(),
            time:ms[0]*60+ms[1]*1
        };
        Tick(StartTime[id]);
    }
}
function Tick(o){
        var now=Math.floor(o.time-(new Date()-o.srt)/1000);
        if (now>=0){
            tickPenalty();
            o.obj.value=Nu(Math.floor(now/60))+':'+Nu(now%60);
            o.to=setTimeout(function(){ Tick(o); },1000);
        }
}
function Nu(nu){
        return (nu>9?'':'0')+nu;
}
function ResetTime(id){
        document.getElementById(id).value = "20:00";
}
function GetTime(id){
        var time = document.getElementById(id).value;
        return time;
}


//Funtions to Select and Remove players as the enter and exit the ice

//players off the ice that haven't been replaced
var homeQueue = new Queue();
var awayQueue = new Queue();
//number of players on ice
var homeSelected = 0;
var awaySelected = 0;
//players currently on ice
var homeOnIce = [];
var awayOnIce = [];
//penalties
var homePenalty = [];
var awayPenalty = [];
//major penalties
var homeMajorPenalty = [];
var awayMajorPenalty = [];
//coincidental penalties
var homeCoincidentalPenalty = [];
var awayCoincidentalPenalty = [];
//max number of players on the ice
var maxHome = 5;
var maxAway = 5;

function SelectPlayer(player, team){
        var time = GetTime('time');
        var obj = document.getElementById(player);
        if (obj.className == "unselected"){
                if (team == "home" && homeSelected <= maxHome){
                        if (!homeQueue.isEmpty()){
                                //remove the player fromt the timeonice queue
                                var outPlayer = homeQueue.dequeue();

                                //add player to the on ice tracker
                                homeOnIce.push(obj.value);


                            //send outplayer and 'inplayer'
                            console.log(time);	//time the switch occured
                            console.log(outPlayer);	//player leaving the ice
                            console.log(obj.value);	//player entering the ice
                            $.post("/events/swap-players", {
                                gameId: GAME_ID,
                                time: timeToStamp(time),
                                outPlayer: outPlayer,
                                inPlayer: obj.value
                            }).done(function(data){console.log(data);});
                        }
                        else { //The first time all the players have been on the ice
                                console.log(obj.value);
                                console.log(time);
                                homeOnIce.push(obj.value);
                        }
                        console.log(homeQueue.isEmpty());
                        obj.className = "selected";
                        homeSelected += 1;
                }
                if (team == "away" && awaySelected <= maxAway){
                        if (!awayQueue.isEmpty()){

                            //remove the player from the timeonice queue
                            var outPlayer = awayQueue.dequeue();

                            //add our player to the on ice tracker
                            awayOnIce.push(obj.value);

                            //send outplayer and 'inplayer'
                            console.log(time);	//time the switch occured
                            console.log(outPlayer);	//player leaving the ice
                            console.log(obj.value);	//player entering the ice
                            $.post("/events/swap-players", {
                                gameId: GAME_ID,
                                time: timeToStamp(time),
                                outPlayer: outPlayer,
                                inPlayer: obj.value
                            }).done(function(data){console.log(data);});;
                        }
                        else { //The first time all the players have been on the ice
                                console.log(obj.value);
                                console.log(time);
                                awayOnIce.push(obj.value);
                        }
                        obj.className = "selected";
                        awaySelected += 1;
                }
        }
        else {
                if (team == "away"){
                        awaySelected -= 1;
                        //remove the outplayer from our ice tracker
                        var index = awayOnIce.indexOf(outPlayer)
                        awayOnIce.splice(index, 1);
                        //add the player to the timeonice queue and players on ice
                        awayQueue.enqueue(obj.value);
                }
                else {
                        homeSelected -= 1;
                        //remove the outplayer from our ice tracker
                        var index = homeOnIce.indexOf(outPlayer)
                        homeOnIce.splice(index, 1);
                        //add the player to the timeonice queue and players on ice
                        homeQueue.enqueue(obj.value);
                }
                obj.className = "unselected";
        }
}


//Log a Penalty
var penaltyTime = "NULL";

//Function to "tick" the penalties and remove them when they are done.
function tickPenalty(){
console.log("TICK");
        var time = GetTime('time');
        var realTime = parseInt(time.substring(0,2))*100 + parseInt(time.substring(3)); //Get the game clock time.
        if (homePenalty.length > 0){
                for (var i = 0; i < homePenalty.length; i++){
                        if (realTime == homePenalty[i][1]){
                                console.log("REMOVING PENALTY");
                                homePenalty.shift();
                        }
                }
        }
        if (homeMajorPenalty.length > 0){
                for (var i = 0; i < homeMajorPenalty.length; i++){
                        if (realTime == homeMajorPenalty[i][1]){
                                homeMajorPenalty.shift();
                        }
                }
        }
        if (homeCoincidentalPenalty.length > 0){
                for (var i = 0; i < homeCoincidentalPenalty.length; i++){
                        if (realTime == homeCoincidentalPenalty[i][1]){
                                homeCoincidentalPenalty.shift();
                        }
                }
        }
        if (awayPenalty.length > 0){
                for (var i = 0; i < homePenalty.length; i++){
                        if (realTime == homePenalty[i][1]){
                                homePenalty.shift();
                        }
                }
        }
        if (awayMajorPenalty.length > 0){
                for (var i = 0; i < awayMajorPenalty.length; i++){
                        if (realTime == awayMajorPenalty[i][1]){
                                awayMajorPenalty.shift();
                        }
                }
        }
        if (awayCoincidentalPenalty.length > 0){
                for (var i = 0; i < awayCoincidentalPenalty.length; i++){
                        if (realTime == awayCoincidentalPenalty[i][1]){
                                awayCoincidentalPenalty.shift();
                        }
                }
        }
}

function penalty(div){
        penaltyTime = GetTime('time');
        popup(div);
}

function HomePenalty(div, Player, Penalty, Length, Coincidental, Major){
        popup(div);

        Length =  parseInt(document.getElementById(Length).value);
        Player =  parseInt(document.getElementById(Player).value);
        Penalty = document.getElementById(Penalty).value;

        //Determine the type of penalty so we know how to deal with it.
        var Type = "";
        if (document.getElementById(Coincidental).checked){
                Type = "Coincidental";
        }
        else if (document.getElementById(Major).checked){
                Type = "Major";
        }
        else {
                Type = "Normal";
        }

        //Determine when the penalty will end.
        var penaltyLength = parseInt(penaltyTime.substring(0,2)) - Length;
        if (penaltyLength < 0){
                penaltyLength += 20;
        }
        var penaltyLength = penaltyLength*100 + parseInt(penaltyTime.substring(3)); //Stores as a number only (removes ":").

        //If the penalty is delayed adjust the out time. Max penalties at once is 2. Currently only handles a third penalty.
        var nextPenalty = 0;
        var realTime = parseInt(penaltyTime.substring(0,2))*100 + parseInt(penaltyTime.substring(3)); //Get the game clock time.
        if (homePenalty.length + homeMajorPenalty.length >= 2){
                if (homePenalty.length > 0){
                        nextPenalty = homePenalty[0][1];
                }
                if (homeMajorPenalty.length > 0){
                        var majorPenaltyTime = homeMajorPenalty[0][1];
                        if (majorPenaltyTime > nextPenalty && majorPenaltyTime > realTime){
                                nextPenalty = majorPenaltyTime;
                        }
                }
                penaltyLength -= (realTime - nextPenalty);
                if (penaltyLength < 0){
                        penaltyLength += 20;
                }
        }

        // console.log(Player);              //Who the player was.
        // console.log(Penalty);             //What the penalty was.
        // console.log(penaltyTime);         //The time the penalty was assessed.
        // console.log(Length);              //How long the peanlty is.
        // console.log(penaltyLength);       //When the player will be back on the ice.
        // console.log(Type);                //The Type of penalty.
        $.post("/events/penalty", {
            gameId: GAME_ID,
            time: timeToStamp(penaltyTime),
            playerId: lookupPlayer(HOME, Player).id,
            penalty: Penalty,
            type: Type,
            length: Length
        });
        var player = [Player, penaltyLength, Length];     //Store the player and penalty so that we can block players from being selected.

        //Add the penalty to the correct penalty array.
        if (Type == "Normal"){
                homePenalty.push(player);
        }
        else if (Type == "Major"){
                homeMajorPenalty.push(player);
        }
        else {
                homeCoincidentalPenalty.push(player);
        }

}

function AwayPenalty(div, Player, Penalty, Length, Coincidental, Major){
        popup(div);

        Length = parseInt(document.getElementById(Length).value);
        Player = parseInt(document.getElementById(Player).value);
        Penalty = document.getElementById(Penalty).value;

        //Determine the type of penalty so we know how to deal with it.
        var Type = "";
        if (document.getElementById(Coincidental).checked){
                Type = "Coincidental";
        }
        else if (document.getElementById(Major).checked){
                Type = "Major";
        }
        else {
                Type = "Normal";
        }

        //Determine when the penalty will end.
        var penaltyLength = parseInt(penaltyTime.substring(0,2)) - Length;
        if (penaltyLength < 0){
                penaltyLength += 20;
        }
        var penaltyLength = penaltyLength*100 + parseInt(penaltyTime.substring(3)); //Stores as a number only (removes ":").

        //If the penalty is delayed adjust the out time. Max penalties at once is 2. Currently only handles a third penalty.
        var nextPenalty = 0;
        var realTime = parseInt(penaltyTime.substring(0,2))*100 + parseInt(penaltyTime.substring(3)); //Get the game clock time.
        if (awayPenalty.length + awayMajorPenalty.length >= 2){
                if (awayPenalty.length > 0){
                        nextPenalty = awayPenalty[0][1];
                }
                if (awayMajorPenalty.length > 0){
                        var majorPenaltyTime = awayMajorPenalty[0][1];
                        if (majorPenaltyTime > nextPenalty && majorPenaltyTime > realTime){
                                nextPenalty = majorPenaltyTime;
                        }
                }
                penaltyLength -= (realTime - nextPenalty);
                if (penaltyLength < 0){
                        penaltyLength += 20;
                }
        }

        // console.log(Player);              //Who the player was.
        // console.log(Penalty);             //What the penalty was.
        // console.log(penaltyTime);         //The time the penalty was assessed.
        // console.log(Length);              //How long the peanlty is.
        // console.log(penaltyLength);       //When the player will be back on the ice.
        // console.log(Type);                //The Type of penalty.
        $.post("/events/penalty", {
            gameId: GAME_ID,
            time: timeToStamp(penaltyTime),
            playerId: lookupPlayer(AWAY, Player).id,
            penalty: Penalty,
            type: Type,
            length: Length
        });
        var player = [Player, penaltyLength, Length];     //Store the player and penalty so that we can block players from being selected.

        //Add the penalty to the correct penalty array.
        if (Type == "Normal"){
                awayPenalty.push(player);
        }
        else if (Type == "Major"){
                awayMajorPenalty.push(player);
        }
        else {
                awayCoincidentalPenalty.push(player);
        }

}


//Log a Goal
var goalTime = "NULL";
function goal(div){
        goalTime = GetTime('time');
        popup(div);
}

function HomeGoal(div, Scorer, Assist1, Assist2){
    popup(div);
    var scorer =  parseInt(document.getElementById(Scorer).value);
    var assist1 = parseInt(document.getElementById(Assist1).value);
    var assist2 = parseInt(document.getElementById(Assist2).value);
    var assists = $.grep([assist1, assist2], function(a) {return !isNaN(a);});
    $.post("/events/goal", {
        gameId: GAME_ID,
        time: timeToStamp(goalTime),
        playerId: lookupPlayer(HOME, scorer).id,
        assists: $.map(assists, function(a) {return lookupPlayer(HOME, a).id;})
    });
    //Log the Goal Scorer and ther Assists with the Time
    // console.log(goalTime);
    // console.log(scorer);
    // console.log(assist1); //Will be empty if no assist
    // console.log(assist2); //Will be empty if no assist
    // console.log(assists);
    //Send the players who were on ice that get a "-".
    console.log(awayOnIce);
    //Send the players who were on ice that get a "+".
    console.log(homeOnIce);
}

function AwayGoal(div, Scorer, Assist1, Assist2){
    popup(div);
    var scorer =  parseInt(document.getElementById(Scorer).value);
    var assist1 = parseInt(document.getElementById(Assist1).value);
    var assist2 = parseInt(document.getElementById(Assist2).value);
    var assists = $.grep([assist1, assist2], function(a) {return !isNaN(a);});
    //Log the Goal Scorer and ther Assists with the Time
    $.post("/events/goal", {
        gameId: GAME_ID,
        time: timeToStamp(goalTime),
        playerId: lookupPlayer(AWAY, scorer).id,
        assists: $.map(assists, function(a) {return lookupPlayer(AWAY, a).id;})
    });
    // console.log(goalTime);
    // console.log(scorer);
    // console.log(assist1); //Will be empty if no assist
    // console.log(assist2); //Will be empty if no assist
    //Send the players who were on ice that get a "-".
    console.log(homeOnIce);
    //Send the players who were on ice that get a "+".
    console.log(awayOnIce);
}

function timeToStamp(time) {
    var min = parseInt(time.substring(0,2));
    var sec = parseInt(time.substring(3));
    return (1200 - (min * 60 + sec));
}

//Log a shot for the home or away team
var shotTime = "NULL";
function shot(div){
        shotTime = GetTime('time');
        popup(div);
}

function lookupPlayer(team, pnum) {
    return $.grep(team.players, function(p) {
        return p.number === pnum;
    })[0];
}

function AwayShot(div, id) {
    popup(div);
    var player = parseInt(document.getElementById(id).value);
    var playerId = $.grep(AWAY.players, function(p) {
        return p.number === player;
    })[0].id;
    // console.log(playerId);
    $.post("/events/shot", {
        gameId: GAME_ID,
        time: timeToStamp(shotTime),
        playerId: playerId
    }).done(function(data) {console.log(data);});
    // console.log("Away Shot");
    // console.log(player);
    // console.log(shotTime);
    document.getElementById(id).value = "";
}

function HomeShot(div, id) {
    popup(div);
    var player = parseInt(document.getElementById(id).value);
    console.log(player);
    var playerId = $.grep(HOME.players, function(p) {
        return p.number === player;
    })[0].id;
    // console.log(playerId);
    $.post("/events/shot", {
        gameId: GAME_ID,
        time: timeToStamp(shotTime),
        playerId: playerId
    }).done(function(data) {console.log(data);});
    // console.log("Home Shot");
    // console.log(player);
    // console.log(shotTime);
    document.getElementById(id).value = "";
}


function loadPlayers() {
    $.getJSON("/teams/"+HOME.id+"/get-roster", function(data) {
        HOME.players = data.data;
                // $.each(data.data, function(key, val) {
                //         console.log(key);
                //         $("#home"+(key+1)).attr("value", val);
                // });
    });
    $.getJSON("/teams/"+AWAY.id+"/get-roster", function(data) {
        AWAY.players = data.data;
    });
}

$(loadPlayers);
