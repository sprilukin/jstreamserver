<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="${pageContext.request.contextPath}/css/jstreamserver.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="${pageContext.request.contextPath}/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css" media="screen"/>

    <script src="${pageContext.request.contextPath}/script/underscore.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/jquery-1.7.1.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/jquery.touch.to.mouse.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/jquery-ui-1.8.16.custom.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/jquery.subtitles.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/backbone.js" type="text/javascript"></script>
    <script src="${pageContext.request.contextPath}/script/jstreamserver.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            JStreamServer.contextPath = "${pageContext.request.contextPath}";

            var folders = <%= (new ObjectMapper()).writeValueAsString(request.getAttribute("folder"))%>;
            new JStreamServer.DirectoryView(folders);

            var breadCrumbs = <%= (new ObjectMapper()).writeValueAsString(request.getAttribute("breadCrumbs"))%>;
            new JStreamServer.BreadCrumbView(breadCrumbs);
        });
    </script>

    <%--HTML templates--%>
    <%@include file="../templates/dirlisttmpl.html"%>
    <%@include file="../templates/breadcrumbtmpl.html"%>
    <%@include file="../templates/videotagtmpl.html"%>
</head>
<body>

<%-- Main content--%>

<div class="container">
    <%@include file="../templates/directory.html"%>
</div>
</body>
</html>
