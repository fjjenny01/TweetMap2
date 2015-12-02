/*Keyword.js*/

var data_all = [];
for (var i = 0; i < data_water.length; i++){
	data_all.push(data_water[i]);
}
for (var i = 0; i < data_run.length; i++){
	data_all.push(data_run[i]);
}
for (var i = 0; i < data_dance.length; i++){
	data_all.push(data_dance[i]);
}
for (var i = 0; i < data_ball.length; i++){
	data_all.push(data_ball[i]);
}
var data_db = data_all;

var keyword = 'all';

function set_keyword(w) {
	keyword = w;
	pcate = document.getElementById("cate");
	switch (w){
	case 'water':
		data_db = data_water;
		pcate.innerHTML = "Keyword: Water Sports";
		break;
	case 'run':
		data_db = data_run;
		pcate.innerHTML = "Keyword: Running";
		break;
	case 'dance':
		data_db = data_dance;
		pcate.innerHTML = "Keyword: Dance";
		break;
	case 'ball':
		data_db = data_ball;
		pcate.innerHTML = "Keyword: Ball Games";
		break;
	case 'all':
		data_db = data_all;
		pcate.innerHTML = "Keyword: All Sports";
		break;
	case 'any':
		data_db = [];
		break;
	}
	pcate.innerHTML += '<span class="caret"></span>';
	//writeToScreen(w);
	load_db();
	doSend(w);
}