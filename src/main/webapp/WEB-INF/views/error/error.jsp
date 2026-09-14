<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>${statusCode} 오류 · QA로그</title>
<style>
  body {
    margin:0; min-height:100vh; display:flex; align-items:center; justify-content:center;
    background:#F7F8FA; color:#171A1F; font-family:"Noto Sans KR","Malgun Gothic",sans-serif;
  }
  .box { text-align:center; padding:40px; }
  .brand { font-weight:700; font-size:14px; color:#565C66; margin-bottom:24px; }
  .code { font-size:56px; font-weight:800; color:#2F6FED; line-height:1; margin-bottom:12px; }
  .message { font-size:16px; color:#171A1F; margin-bottom:24px; }
  .home-link {
    display:inline-block; font-size:13.5px; font-weight:600; color:#FFFFFF; background:#2F6FED;
    padding:10px 20px; border-radius:8px; text-decoration:none;
  }
  .home-link:hover { opacity:.92; }
</style>
</head>
<body>
  <div class="box">
    <div class="brand">QA로그</div>
    <div class="code">${statusCode} 오류입니다</div>
    <div class="message">${message}</div>
    <a class="home-link" href="${ctx}/">홈으로 돌아가기</a>
  </div>
</body>
</html>
