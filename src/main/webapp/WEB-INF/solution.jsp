<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:frame title="${description} - Hint and Solution">
    <link rel="stylesheet" media="screen" href='/stylesheets/prettify-skin-desert-trunk.css'>
    <script src='/prettify-small-4-Mar-2013/google-code-prettify/prettify.js'></script>

    <h4>PROBLEM INFO</h4>
    <ul>
        <li>${description}</li>
    </ul>

    <h4>ATTENTION</h4>
    <ul>
        <li class="text-warning">Before see the hints, Try to solve by yourself.</li>
        <li class="text-warning">Before see the source code, Try to solve by yourself.</li>
    </ul>
    <h4>HINTS</h4>
    <ul>
        <c:forEach var="item" items="${hints}">
            <li>${item}</li>
        </c:forEach>
    </ul>

    <h4>SOURCE CODE</h4>
    <pre class="small prettyprint lang-java small-tab">${sourceCode}</pre>
    <p>
        For simplicity and to use the common algorithms, this solution is using
        psjava library (<a href="http://psjava.org">http://psjava.org</a>)
    </p>

    <script>
        prettyPrint();
    </script>
</t:frame>
