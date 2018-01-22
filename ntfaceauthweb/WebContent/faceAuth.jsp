<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

    <script src="https://sdk.amazonaws.com/js/aws-sdk-2.163.0.min.js"></script>
    <script src="./app.js"></script>  
    <script>
       function getHtml(template) {
          return template.join('\n');
       }
        listAlbums();
    </script>
        
</head>
  <body>
    <h1 style="color:blue">NexGen Technocrats - FacialAuthentication App</h1>
    <div id="app" style="background-color:orange" style="color:snow"></div> 
    
  </body>
</html>