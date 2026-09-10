<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>QA로그 - 스캐폴딩 확인</title>
</head>
<body style="font-family:'Malgun Gothic',sans-serif; padding:32px;">
    <h1>QA로그 스캐폴딩 정상 동작</h1>
    <p>Controller → Service → MyBatis Mapper → DB 배선이 정상이면 아래에 projects 테이블 내용이 보입니다.
       (이 화면은 실제 메인 화면이 아니라 배선 확인용 임시 페이지입니다.)</p>
    <table border="1" cellpadding="6" cellspacing="0">
        <tr><th>id</th><th>name</th><th>미종료 오류 수</th><th>생성일</th></tr>
        <c:forEach var="p" items="${projects}">
            <tr>
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${p.openIssueCount}</td>
                <td>${p.createdAt}</td>
            </tr>
        </c:forEach>
    </table>
    <c:if test="${empty projects}">
        <p>등록된 프로젝트가 없습니다. <code>POST /api/projects</code>로 하나 만들어 보거나 DB에 직접 넣어보세요.</p>
    </c:if>
</body>
</html>
