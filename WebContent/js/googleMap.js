/*GoogleMap.js*/
var map, heatmap, geos = [], geos_his = [];


function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
	 
    zoom: 4,
    center: {lat: 37.775, lng: -122.434},
    mapTypeId: google.maps.MapTypeId.SATELLITE
  });

  heatmap = new google.maps.visualization.HeatmapLayer({
	  maxIntensity: 30,
	  dissipating: true,
    data: [],
    radius: 7,
    map: map
  });
  load_db();
  changeGradient();
}

function toggleHeatmap() {
  heatmap.setMap(heatmap.getMap() ? null : map);
}

function changeGradient() {
  var gradient = [
    'rgba(0, 255, 255, 0)',
    'rgba(0, 255, 255, 1)',
    'rgba(0, 191, 255, 1)',
    'rgba(0, 127, 255, 1)',
    'rgba(0, 63, 255, 1)',
    'rgba(0, 0, 255, 1)',
    'rgba(0, 0, 223, 1)',
    'rgba(0, 0, 191, 1)',
    'rgba(0, 0, 159, 1)',
    'rgba(0, 0, 127, 1)',
    'rgba(63, 0, 91, 1)',
    'rgba(127, 0, 63, 1)',
    'rgba(191, 0, 31, 1)',
    'rgba(255, 0, 0, 1)'
  ]
  heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}

function changeRadius() {
  heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
  heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}
function addGeo(coordinate) {
	var lat = parseFloat(coordinate[0]);
	var lon = parseFloat(coordinate[1]);
	geos.push(new google.maps.LatLng(lat, lon));
	geos_his.push(new google.maps.LatLng(lat, lon));
	//writeToScreen(coordinate[0]+","+coordinate[1]);
	heatmap.setData(new google.maps.MVCArray(geos));
}
function show_history() {
	geos = geos_his;
	heatmap.set('radius', 3);
	heatmap.set('maxIntensity', 50);
	heatmap.setData(new google.maps.MVCArray(geos));
}
function hide_history() {
	geos = [];
	heatmap.set('radius', 10);
	heatmap.set('maxIntensity', 5);
	heatmap.setData(new google.maps.MVCArray(geos));
}

function load_db() {
	geos = []; geos_his = [];
	for (var i = 0; i < data_db.length; i++){
		geos.push(new google.maps.LatLng(data_db[i].lat, data_db[i].lon));
		geos_his.push(new google.maps.LatLng(data_db[i].lat, data_db[i].lon));
	}
	//writeToScreen(geos.length.toString());
	heatmap.setData(new google.maps.MVCArray(geos));
}