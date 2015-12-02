var data_tot = {'Ball Games':data_ball, 'Running': data_run, 'Dance': data_dance, 'Water Sports': data_water};
var date_list = {'Ball Games':{}, 'Running': {}, 'Dance': {}, 'Water Sports': {}};
var sports = ['Ball Games', 'Running', 'Dance', 'Water Sports'];
var n_dates = 0;
var dates = [];
var time_list = {'Ball Games':{}, 'Running': {}, 'Dance': {}, 'Water Sports': {}};
var times = []
for (var j = 0; j < 24; ++j){
	if (j < 10){
		times.push('0'+j.toString());
	}
	else{
		times.push(j.toString());
	}
}
for (var i = 0; i < 4; ++i){
	//writeToScreen(sports[i]);
	for (var j = 0; j < 24; ++j){
		if (j < 10){
			time_list[sports[i]]['0'+j.toString()] = 0;
		}
		else{
			time_list[sports[i]][j.toString()] = 0;
		}
	}
	n_dates = 0;
	dates = [];
	for (var j = 0; j < data_tot[sports[i]].length; ++j){
		st = data_tot[sports[i]][j].time.split(' ');
		date = st[0]+' '+st[1]+' '+st[2];
		time = st[3].split(':')[0];
		if (date_list[sports[i]].hasOwnProperty(date)){
			date_list[sports[i]][date] += 1;
		}
		else{
			date_list[sports[i]][date] = 0;
			n_dates += 1;
			dates.push(date);
		}
		time_list[sports[i]][time] += 1;

	}
}

var dates_tot = {};
for (var i = 0; i < dates.length; ++i){
	dates_tot[dates[i]] = 0;
	for (j = 0; j < sports.length; ++j){
		dates_tot[dates[i]] += date_list[sports[j]][dates[i]];
	}
}
var date_id = 0;
/*

writeToScreen(dates.length.toString());
for (var i= 0; i < n_dates; ++i){
	for (var j = 0; j < 4; ++j){
		writeToScreen(sports[j]+'-'+dates[i]+'-'+date_list[sports[j]][dates[i]]);
	}
}
*/