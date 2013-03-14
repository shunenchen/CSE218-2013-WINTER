function EpochToDate(epoch) {
        var date = new Date(epoch *1000);
    return date.toLocaleDateString();
}

function MillisecToMinSec(sec) {
        var x = sec;
        var seconds = x % 60;
        x /= 60
        var minutes = x;

        var ret = {};
        minutes = Math.floor(minutes).toString();
        if (minutes.length === 1) {
            minutes = "0" + minutes;
        }
        seconds = Math.floor(seconds).toString();
        if (seconds.length === 1) {
            seconds = "0" + seconds;
        }
        ret["mins"] = minutes;
        ret["secs"] = seconds;

        return ret;
}

