<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="/css/jstreamserver.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css" media="screen"/>

    <script src="/script/underscore.js" type="text/javascript"></script>
    <script src="/script/jquery-1.7.1.js" type="text/javascript"></script>
    <script src="/script/jquery.touch.to.mouse.js" type="text/javascript"></script>
    <script src="/script/jquery-ui-1.8.16.custom.js" type="text/javascript"></script>
    <script src="/script/jquery.subtitles.js" type="text/javascript"></script>
    <script src="/script/backbone.js" type="text/javascript"></script>
    <script src="/script/jstreamserver.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            new JStreamServer.DirectoryView(${files});
            new JStreamServer.BreadCrumbView(${breadCrumbs})
        });
    </script>

    <%--HTML templates--%>
    <%@include file="dirlisttmpl.html"%>
    <%@include file="breadcrumbtmpl.html"%>
    <%@include file="videotagtmpl.html"%>
</head>
<body>

<%-- Main content--%>

<div class="container">
    <%@include file="directory.html"%>
</div>
</body>
</html>
