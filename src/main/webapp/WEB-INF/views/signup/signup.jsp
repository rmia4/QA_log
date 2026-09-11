<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>회원가입 | QA로그</title>
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
        <section class="login-card signup-card" aria-labelledby="signup-title">
            <div class="card-accent" aria-hidden="true"></div>
            <div class="card-body">
                <span class="eyebrow">JOIN QA LOG</span>
                <h1 id="signup-title">회원가입</h1>
                <p class="description">팀에서 공유받은 초대코드로 업무 공간에 참여하세요.</p>

                <form action="<c:url value='/signup' />" method="post">
                    <div class="field">
                        <label for="invite_code">팀 초대코드</label>
                        <input id="invite_code" name="invite_code" type="password"
                               autocomplete="off" placeholder="초대코드를 입력하세요" required autofocus>
                    </div>
                    <div class="field">
                        <label for="login_id">아이디</label>
                        <input id="login_id" name="login_id" type="text"
                               value="<c:out value='${loginId}' />" maxlength="50"
                               autocomplete="username" autocapitalize="none"
                               placeholder="로그인에 사용할 아이디" required>
                    </div>
                    <div class="field">
                        <label for="password">비밀번호</label>
                        <input id="password" name="password" type="password"
                               autocomplete="new-password" placeholder="비밀번호를 입력하세요" required>
                    </div>
                    <div class="field">
                        <label for="display_name">화면 표시 이름</label>
                        <input id="display_name" name="display_name" type="text"
                               value="<c:out value='${displayName}' />" maxlength="50"
                               autocomplete="nickname" placeholder="팀원에게 표시할 이름" required>
                    </div>

                    <c:if test="${not empty signupError}">
                        <p class="login-error" role="alert"><c:out value="${signupError}" /></p>
                    </c:if>

                    <button class="login-button" type="submit">가입하고 시작하기</button>
                </form>

                <div class="account-link">
                    <span>이미 계정이 있나요?</span>
                    <a href="<c:url value='/login' />">로그인</a>
                </div>
            </div>
        </section>
    </main>

    <footer>QA로그 · 팀 전용 오류 관리</footer>
</body>
</html>
