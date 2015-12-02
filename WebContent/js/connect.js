/*WebSocket.js*/
var loc = window.location, wsUri;

if (loc.protocol === "https:") {
    wsUri = "wss:";
} 
else {
    wsUri = "ws:";
}
//wsUri += "//160.39.142.111:8080/TweetMap/echo";
wsUri += "//" + "54.152.22.99:8080/echo";
//wsUri = "ws://f2e95bef.ngrok.io/TweetMap/echo";
//wsUri += "//" + loc.host;
//wsUri += loc.pathname + "echo";

var output = document.getElementById("output");
function init() {
    
  //writeToScreen(wsUri);
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
        onOpen(evt)
    };
    websocket.onmessage = function(evt) {
        onMessage(evt)
    };
    websocket.onerror = function(evt) {
        onError(evt)
    };
}
function send_message() {
}
function onOpen(evt) {
    //writeToScreen("Connected to Endpoint!");
}
insc = 10;
var texts = [];
for (var i = 0; i < insc; ++i){
	texts.push("")
}
nextPut = 0;
popo = 0;
nene = 0;
nono = 0;
function onMessage(evt) {
//	writeToScreen(evt.data);
	if (evt.data[0]=='$'){
		if (texts[nextPut] != ''){
			if (texts[nextPut].search("positive") >= 0){
				popo = popo-1;
			}
			else if (texts[nextPut].search("negative") >= 0){
				nene = nene-1;
			}
			else{
				nono = nono-1;
			}
		}
		texts[nextPut] = evt.data.substring(1,evt.data.length);
		if (texts[nextPut].search("positive") >= 0){
			popo = popo+1;
		}
		else if (texts[nextPut].search("negative") >= 0){
			nene = nene+1;
		}
		else{
			nono = nono+1;
		}
		output.innerHTML = "";
		writeToScreen("Sentiment distribution for last 10 tweets:");
		writeToScreen("Positve: "+popo.toString()+" Negative: "+nene.toString()+" Netural: "+nono.toString());
		pp = nextPut
		for (var i=0; i <insc; ++i){
			writeToScreen(texts[pp]);
			pp = pp-1;
			if (pp == -1){
				pp = insc-1;
			}
		}
		nextPut = (nextPut+1)%insc;	
		
		
	}
		
	else if (evt.data.indexOf(',') > 0){
		//writeToScreen(evt.data);
		var coordinate = evt.data.split(",");
		addGeo(coordinate);	
	}
	
    //writeToScreen("Message Received: " + evt.data);
}
function onError(evt) {
    //writeToScreen('ERROR: ' + evt.data);
}
function doSend(message) {
    //writeToScreen("Keyword Sent: " + message);
    websocket.send(message);
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    if (message != ''){
    	output.appendChild(pre);
    }
    
    
}

window.addEventListener("load", init, false);
//window.addEventListener("load", load_db, false);




