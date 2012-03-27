<%@ taglib prefix="spring" uri="/spring" %>
<%@ page isErrorPage="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>
<body>
<h1>An exception occured during last request:</h1>
<pre>${exception}</pre>
</body>
</html>
