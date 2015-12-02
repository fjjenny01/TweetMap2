var margin = {top: 20, right: 80, bottom: 30, left: 50},
    width = 960 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var parseDate = d3.time.format("%Y%m%d").parse;

var x = d3.scale.linear()
    .range([0, width]);

var y = d3.scale.linear()
    .range([height, 0]);

var ccolor = d3.scale.category10().domain(['Water Sports', 'Running', 'Ball Games', 'Dance']);


var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");

var line = d3.svg.line()
    //.interpolate("basis")
    .x(function(d) { return x(d.time); })
    .y(function(d) { return y(d.value); });

var svgg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
var timesToPlot = ccolor.domain().map(function(name) {
    return {
      name: name,
      values: times.map(function(time) {
        return {time: time, value: time_list[name][time]};
      })
    };
  });



x.domain(d3.extent(times));

y.domain([
  d3.min(timesToPlot, function(c) { return d3.min(c.values, function(v) { return v.value; }); }),
  d3.max(timesToPlot, function(c) { return d3.max(c.values, function(v) { return v.value; }); })
]);

svgg.append("g")
.attr("class", "x axis")
.attr("transform", "translate(0," + height + ")")
.call(xAxis);

svgg.append("g")
.attr("class", "y axis")
.call(yAxis)
.append("text")
.attr("transform", "rotate(-90)")
.attr("y", 6)
.attr("dy", ".71em")
.style("text-anchor", "end")
.text("Tweets");





var ttp = svgg.selectAll(".timesToPlot")
.data(timesToPlot)
.enter().append("g")
.attr("class", "timesToPlot");

ttp.append("path")
.attr("class", "line")
.attr("d", function(d) { return line(d.values); })
.attr("fill", "none")
.attr("data-legend",function(d) { return d.name})
.style("stroke", function(d) { return ccolor(d.name); });
/*
ttp.append("text")
.datum(function(d) { return {name: d.name, value: d.values[d.values.length - 1]}; })
.attr("transform", function(d) { return "translate(" + x(d.value.time) + "," + y(d.value.value) + ")"; })
.attr("x", 3)
.attr("dy", ".35em")
.text(function(d) { return d.name; });
*/

legend = svgg.append("g")
.attr("class","legend")
.attr("fill", "none")
.attr("transform","translate(50,30)")
.style("font-size","12pt")
.call(d3.legend);
/*
setTimeout(function() { 
legend
  .style("font-size","20px")
  .attr("data-style-padding",10)
  .call(d3.legend)
},1000);
*/
