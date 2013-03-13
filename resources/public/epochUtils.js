function EpochToDate(epoch) {
	var date = new Date(epoch *1000);
    return date.toLocaleDateString();
}

function MillisecToMinSec(sec) {
	var x = sec;
	var seconds = x % 60;
	x /= 60
	var minutes = x % 60;
	
	var ret = {};
	ret["mins"] = Math.floor(minutes);
	ret["secs"] = Math.floor(seconds);
	
	return ret;
}

