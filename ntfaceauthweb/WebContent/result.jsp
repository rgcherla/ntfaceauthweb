<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<!DOCTYPE html>
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Face Detection Results</title>

<style type="text/css">
	
	#faceMatchResult {
		height:500px;
		length:500px;
		border:2px solid black;
		background-color:silver;
		margin:30px;
		font-size:150%;
	}
	
	#headerDetails {
		color: blue;
	}
	
	.spanStyle {
		color: red;
	}
	
</style>

</head>
<body>
	
	<div id="faceMatchResult" style="background-color:orange">
		<span id="headerDetails" style="color:blue"><b>User Authenticated</b>
		<br>
		<br>
		<br>
		Face match(es) found :
		</span>
	 	
	 	<c:forEach var="faceMatch" items="${faceMatches}">
		   <br><span class="spanStyle">Confidence level : <c:out value="${faceMatch.getConfidence()}"/></span>
		   <br><span class="spanStyle">Customer Source Image : <c:out value="${faceMatch.getCustomerImageName()}"/></span>
		   <br><span class="spanStyle">Matched Image : </span><c:out value="${faceMatch.getMatchedFace().getExternalImageId()}"/><br>
		</c:forEach> 
	</div>

</body>
</html>