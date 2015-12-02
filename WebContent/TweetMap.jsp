<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@  page import ="org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">

<head>
	<link rel="icon" href="data:;base64,=">
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>TweetMap</title>

    <!-- Bootstrap Core CSS - Uses Bootswatch Flatly Theme: http://bootswatch.com/flatly/ -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="css/freelancer.css" rel="stylesheet">

	<!--  Pie CSS -->
	<link href="css/piechart.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body  id="page-top" class="index">
    <%!
    public JSONArray get_data(String table) throws ClassNotFoundException, SQLException{
    	Class.forName("com.mysql.jdbc.Driver"); 
    	String usr = "jin";
    	String pwd = "Xsjy2015";
        java.sql.Connection con = DriverManager.getConnection("jdbc:mysql://mydbinstance.cqnhof000vot.us-east-1.rds.amazonaws.com:3306/TweetSchema",
        usr,pwd); 
        java.sql.Statement st= con.createStatement(); 
        ResultSet rs=st.executeQuery("select * from "+table);
        JSONArray arr = new JSONArray();
        JSONObject tmp;
        while (rs.next()){
        	tmp = new JSONObject();
            tmp.put("lat", rs.getFloat("latitude"));
    		tmp.put("lon", rs.getFloat("longtitude"));
    		tmp.put("time", rs.getString("time"));
    		tmp.put("keyword", rs.getString("keyword"));
    		//System.out.println("ll:"+Float.toString(rs.getFloat("latitude"))+":"+Float.toString(rs.getFloat("longtitude")));
            arr.add(tmp);
        }
        return arr;
        }
    %>
    <script type="text/javascript">
    var data_water = <%=get_data("waterSp")%>;
    var data_run = <%=get_data("run")%>;
    var data_dance = <%=get_data("dance")%>;
    var data_ball = <%=get_data("TweeTable")%>;
    //var data_db = data_water;
    </script>
    
    
	<!-- Navigation -->
    <nav class="navbar navbar-default navbar-fixed-top">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#page-top">Tweet Map</a>
            </div>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav navbar-right">
                    <li class="hidden">
                        <a href="#page-top"></a>
                    </li>
                    <li class="page-scroll">
                        <a href="#TweetMap">TweetMap</a>
                    </li>
                    <li class="page-scroll">
                        <a href="#Analysis1">Analysis1</a>
                    </li>
                    <li class="page-scroll">
                        <a href="#Analysis2">Analysis2</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </nav>


    <!-- TweetMap Grid Section -->
    <section id="TweetMap">
        <div class="container">
        	<div class="row">
                <div class="col-lg-12 text-center">
                    <h2>Real Time TweetMap</h2>
                    <hr class="star-primary">
                </div>
            </div>
        	
            <div class="row">
            <div class="col-lg-12 text-center">
            <div class="form-actions">
        	<button class="btn btn-success" onclick="show_history()">Show History</button>
            <button class="btn btn-danger" onclick="hide_history()">Clear History</button>
            </div>
            <div class="btn-group">
			    <button class="btn btn-info dropdown-toggle" type="button" data-toggle="dropdown" id="cate">Keyword: All Sports
			    <span class="caret"></span></button>
			    <ul class="dropdown-menu">
			      <li><a onClick="set_keyword('ball')">Ball Games</a></li>
			      <li><a onClick="set_keyword('water')">Water Sports</a></li>
			      <li><a onClick="set_keyword('dance')">Dance</a></li>
			      <li><a onClick="set_keyword('run')">Running</a></li>
			      <li class="divider"></li>
			      <li><a onClick="set_keyword('all')">All Sports</a></li>
			    </ul>
			    
			  </div>
        
        
            <div id="map"></div>
            <div id="output"></div>
            </div>
            </div>
        </div>
    </section>

    <!-- About Section -->
    <section class="success" id="Analysis1">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 text-center">
                    <h2>Sports Percentage</h2>
                    <hr class="star-light">
                </div>
            </div>
            <div class="row text-center" id="pie">
            </div>
            <div class="row text-center">
            <p id="date"></p>
            </div>
            <div class="row text-center">
            <button class="nextday btn btn-primary">Show Next Day</button>
            </div>
        </div>
    </section>

	<!-- About Section -->
    <section id="Analysis2">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 text-center">
                    <h2>Tweets Post vs Hour</h2>
                </div>
            </div>
            <div class="row text-center" id="curve">
            </div>
        </div>
    </section>



    <!-- Scroll to Top Button (Only visible on small and extra-small screen sizes) -->
    <div class="scroll-top page-scroll visible-xs visible-sm">
        <a class="btn btn-primary" href="#page-top">
            <i class="fa fa-chevron-up"></i>
        </a>
    </div>



    <!-- jQuery -->
    <script src="js/jquery.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="js/bootstrap.min.js"></script>

    <!-- Plugin JavaScript -->
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js"></script>
    <script src="js/classie.js"></script>
    <script src="js/cbpAnimatedHeader.js"></script>


    <!-- Custom Theme JavaScript -->
    <script src="js/freelancer.js"></script>
    
    <script type="text/javascript" src="js/connect.js"></script>
    <script type="text/javascript" src="js/googleMap.js"></script>
    <script type="text/javascript" src="js/keyword.js"></script>
    <script async defer
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCcMiv8OP15ybstwgtkej9ekNikMOpyDRs&libraries=visualization&callback=initMap">
    </script>
    <script type="text/javascript">
    $(window).resize(function () {
        var h = $(window).height(),
            offsetTop = 60; // Calculate the top offset

        $('#map').css('height', (h - offsetTop));
    }).resize();
    </script>
    
    
	<!--<script src="http://d3js.org/d3.v3.js"></script>  -->
	<script src="js/d3.v3.js"></script>
	<!--  <script src="http://d3js.org/d3.v3.min.js"></script> -->
	<script src="js/d3.v3.min.js"></script>
	<script src="js/data_analysis.js"></script>
	<script src="js/piechart.js"></script>
	
	
	<!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>  -->
	<script src="js/d3.min.js"></script>
	<script src="js/d3.legend.js"></script>
	<script src="js/curve.js"></script>
	
</body>

</html>
