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

//Game Timer Functions
function StartTime(id,srt){
	var obj=document.getElementById(id),ms=obj.value.split(/\W/);
	if (StartTime[id]){
		clearTimeout(StartTime[id].to);
	}
	if (srt&&isFinite(ms[0])&&isFinite(ms[1])){
		StartTime[id]={
		obj:obj,
		srt:new Date(),
		time:ms[0]*60+ms[1]*1
	}
	Tick(StartTime[id]);
	}
}
function Tick(o){
	var now=Math.floor(o.time-(new Date()-o.srt)/1000);
	if (now>=0){
		o.obj.value=Nu(Math.floor(now/60))+':'+Nu(now%60);
		o.to=setTimeout(function(){ Tick(o); },1000)
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
var homeQueue = new Queue();
var awayQueue = new Queue();
var homeselected = 0;
var awayselected = 0;
function SelectPlayer(player, team){
	var time = GetTime('time');
	var obj = document.getElementById(player);
	if (obj.className == "unselected"){
		if (team == "home" && homeselected <= 5){
			if (!homeQueue.isEmpty()){
				var outPlayer = homeQueue.dequeue();
				//send outplayer and 'inplayer'
				console.log(time);	//time the switch occured
				console.log(outPlayer);	//player leaving the ice
				console.log(obj);	//player entering the ice
			}
			else { //The first time all the players have been on the ice
				console.log(obj);
				console.log(time);
			}
			console.log(homeQueue.isEmpty());
			obj.className = "selected";
			homeselected += 1;
		}
		if (team == "away" && awayselected <= 5){
			if (!awayQueue.isEmpty()){
				var outPlayer = awayQueue.dequeue();
				//send outplayer and 'inplayer'
				console.log(time);	//time the switch occured
				console.log(outPlayer);	//player leaving the ice
				console.log(obj);	//player entering the ice
			}
			else { //The first time all the players have been on the ice
				console.log(obj);
				console.log(time);
			}
			obj.className = "selected";
			awayselected += 1;
		}
	}
	else {
		if (team == "away"){
			awayselected -= 1;
			awayQueue.enqueue(obj)
		}
		else {
			homeselected -= 1;
			homeQueue.enqueue(obj)
		}
		obj.className = "unselected";
	}
}

//Log a shot for the home or away team
var shotTime = "NULL";
function shot(div){
	shotTime = GetTime('time');
	popup(div);
}

function AwayShot(div, id) {
	popup(div);
	var player = document.getElementById(id).value;
	console.log("Away Shot");
	console.log(player);
	console.log(shotTime);
	document.getElementById(id).value = "";
}

function HomeShot(div, id) {
	popup(div);
	var player = document.getElementById(id).value;
	console.log("Home Shot");
	console.log(player);
	console.log(shotTime);
	document.getElementById(id).value = "";
}

//Function to get the forwards that should be on the ice.
function getForwards() {
	$.getJSON("/teams/foo/get-forwards", function(data) {
		$.each(data.data, function(key, val) {
			console.log(key);
			$("#home"+(key+1)).attr("value", val);
		});
	});
}

$(getForwards);
