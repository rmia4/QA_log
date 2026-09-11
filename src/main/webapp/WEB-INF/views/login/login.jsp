<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>로그인 | QA로그</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/login.css' />">
</head>
<body>
    <header class="site-header">
        <a class="brand" href="<c:url value='/login' />" aria-label="QA로그 로그인">
            <span class="brand-mark" aria-hidden="true">Q</span>
            <span>QA로그</span>
        </a>
        <span class="team-label">팀 업무 공간</span>
    </header>

    <main class="login-main">
        <section class="login-card" aria-labelledby="login-title">
            <div class="card-accent" aria-hidden="true"></div>
            <div class="card-body">
                <span class="eyebrow">QA LOG</span>
                <h1 id="login-title">로그인</h1>
                <p class="description">팀의 오류와 수정 이력을 한곳에서 관리하세요.</p>

                <form action="<c:url value='/login' />" method="post">
                    <div class="field">
                        <label for="login_id">아이디</label>
                        <input id="login_id" name="login_id" type="text"
                               value="<c:out value='${loginId}' />"
                               autocomplete="username" autocapitalize="none"
                               placeholder="아이디를 입력하세요" required autofocus>
                    </div>
                    <div class="field">
                        <label for="password">비밀번호</label>
                        <input id="password" name="password" type="password"
                               autocomplete="current-password"
                               placeholder="비밀번호를 입력하세요" required>
                    </div>

                    <c:if test="${not empty loginError}">
                        <p class="login-error" role="alert"><c:out value="${loginError}" /></p>
                    </c:if>

                    <button class="login-button" type="submit">로그인</button>
                </form>

                <p class="team-note"><span aria-hidden="true"></span>팀에서 만든 계정으로 로그인하세요.</p>
            </div>
        </section>
    </main>

    <footer>QA로그 · 팀 전용 오류 관리</footer>
</body>
</html>
