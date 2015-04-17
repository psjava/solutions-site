<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:frame title="Accepted Solution">
    <h4>SOLUTIONS</h4>
    <ul>
        <c:forEach var="item" items="${solutionInfoList}">
            <li><a href="/${item.siteCode}/${item.problemId}/${item.urlDir}">${item.description}</a></li>
        </c:forEach>
    </ul>
</t:frame>