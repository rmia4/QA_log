<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>프로젝트 오류 관리 | QA로그</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/main.css' />">
</head>
<body>
    <header class="app-header">
        <a class="brand" href="<c:url value='/' />">
            <span class="brand-mark" aria-hidden="true">Q</span>
            <span>QA로그</span>
        </a>
        <div class="header-user">
            <span class="user-avatar" aria-hidden="true"><c:out value="${empty sessionScope.loginDisplayName ? 'U' : fn:substring(sessionScope.loginDisplayName, 0, 1)}" /></span>
            <span class="user-name"><c:out value="${empty sessionScope.loginDisplayName ? '사용자' : sessionScope.loginDisplayName}" /></span>
        </div>
    </header>

    <main class="workspace">
        <aside class="project-panel" aria-label="프로젝트 목록">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">WORKSPACE</span>
                    <h1>프로젝트</h1>
                </div>
                <span class="project-total">${fn:length(projects)}</span>
            </div>

            <nav class="project-list">
                <c:forEach var="project" items="${projects}">
                    <c:url var="projectUrl" value="/">
                        <c:param name="projectId" value="${project.id}" />
                        <c:param name="view" value="${listMode}" />
                    </c:url>
                    <a class="project-item ${project.id == selectedProjectId ? 'is-selected' : ''}"
                       href="${projectUrl}" ${project.id == selectedProjectId ? 'aria-current="page"' : ''}>
                        <span class="project-icon" aria-hidden="true"><c:out value="${fn:substring(project.name, 0, 1)}" /></span>
                        <span class="project-copy">
                            <strong><c:out value="${project.name}" /></strong>
                            <small>미종료 오류 ${project.openIssueCount}건</small>
                        </span>
                        <span class="project-count">${project.openIssueCount}</span>
                    </a>
                </c:forEach>
            </nav>

            <c:if test="${empty projects}">
                <div class="project-empty">
                    <span aria-hidden="true">＋</span>
                    <p>등록된 프로젝트가 없습니다.</p>
                </div>
            </c:if>
        </aside>

        <section class="issue-panel" aria-label="오류 목록">
            <c:choose>
                <c:when test="${not empty selectedProject}">
                    <div class="issue-heading">
                        <div>
                            <p class="breadcrumb">프로젝트 / 오류 관리</p>
                            <h2><c:out value="${selectedProject.name}" /></h2>
                            <p class="heading-description">발견된 오류의 처리 상태와 담당자를 확인하세요.</p>
                        </div>
                    </div>

                    <div class="issue-toolbar">
                        <div class="tabs" role="tablist" aria-label="오류 상태 구분">
                            <c:url var="activeUrl" value="/">
                                <c:param name="projectId" value="${selectedProjectId}" />
                                <c:param name="view" value="active" />
                            </c:url>
                            <c:url var="closedUrl" value="/">
                                <c:param name="projectId" value="${selectedProjectId}" />
                                <c:param name="view" value="closed" />
                            </c:url>
                            <a href="${activeUrl}" class="tab ${listMode == 'active' ? 'is-active' : ''}" role="tab"
                               aria-selected="${listMode == 'active'}">진행 중</a>
                            <a href="${closedUrl}" class="tab ${listMode == 'closed' ? 'is-active' : ''}" role="tab"
                               aria-selected="${listMode == 'closed'}">종료됨</a>
                        </div>
                        <span class="result-count">총 ${fn:length(issues)}건</span>
                    </div>

                    <div class="issue-table-wrap">
                        <table class="issue-table">
                            <thead>
                                <tr>
                                    <th class="number-column">번호</th>
                                    <th>오류 제목</th>
                                    <th>상태</th>
                                    <th>심각도</th>
                                    <th>담당자</th>
                                    <th>최근 수정</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="issue" items="${issues}">
                                    <tr>
                                        <td class="issue-number">#${empty issue.issueNumber ? issue.id : issue.issueNumber}</td>
                                        <td class="issue-title">
                                            <a href="<c:url value='/issues/${issue.id}' />">
                                                <c:out value="${empty issue.title ? '제목 없음' : issue.title}" />
                                            </a>
                                        </td>
                                        <td>
                                            <span class="status status-${issue.status}">
                                                <c:choose>
                                                    <c:when test="${issue.status == 'new'}">신규</c:when>
                                                    <c:when test="${issue.status == 'reviewing'}">확인 중</c:when>
                                                    <c:when test="${issue.status == 'fixing'}">수정 중</c:when>
                                                    <c:when test="${issue.status == 'closed'}">종료</c:when>
                                                    <c:otherwise><c:out value="${issue.status}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="severity severity-${issue.severity}">
                                                <c:choose>
                                                    <c:when test="${issue.severity == 'low'}">낮음</c:when>
                                                    <c:when test="${issue.severity == 'medium'}">보통</c:when>
                                                    <c:when test="${issue.severity == 'high'}">높음</c:when>
                                                    <c:when test="${issue.severity == 'critical'}">치명적</c:when>
                                                    <c:otherwise>미지정</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td><c:out value="${empty issue.assigneeName ? '미지정' : issue.assigneeName}" /></td>
                                        <td class="updated-at"><c:out value="${issue.updatedAt}" /></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>

                        <c:if test="${empty issues}">
                            <div class="issue-empty">
                                <span class="empty-icon" aria-hidden="true">✓</span>
                                <h3>${listMode == 'closed' ? '종료된 오류가 없습니다.' : '진행 중인 오류가 없습니다.'}</h3>
                                <p>${listMode == 'closed' ? '오류를 종료하면 이곳에서 다시 확인할 수 있습니다.' : '이 프로젝트에 등록된 미종료 오류가 없습니다.'}</p>
                            </div>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="workspace-empty">
                        <span class="empty-icon" aria-hidden="true">Q</span>
                        <h2>프로젝트를 먼저 등록해 주세요.</h2>
                        <p>프로젝트가 생성되면 이곳에서 오류를 한 번에 관리할 수 있습니다.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</body>
</html>
