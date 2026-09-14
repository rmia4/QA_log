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
        <div class="header-actions">
            <label class="search-box project-search-box">
                <span class="search-icon" aria-hidden="true">⌕</span>
                <input id="projectSearchInput" class="search-input" type="search"
                       placeholder="프로젝트 검색" autocomplete="off" aria-label="프로젝트 검색">
            </label>
            <button type="button" class="action-button action-button-primary"
                    onclick="document.getElementById('createProjectDialog').showModal()">
                <span aria-hidden="true">＋</span> 프로젝트 생성
            </button>
            <div class="header-user">
                <span class="user-avatar" aria-hidden="true"><c:out value="${empty sessionScope.loginDisplayName ? 'U' : fn:substring(sessionScope.loginDisplayName, 0, 1)}" /></span>
                <span class="user-name"><c:out value="${empty sessionScope.loginDisplayName ? '사용자' : sessionScope.loginDisplayName}" /></span>
                <form class="logout-form" action="<c:url value='/logout' />" method="post">
                    <button type="submit" class="logout-button">로그아웃</button>
                </form>
            </div>
        </div>
    </header>

    <main class="workspace">
        <aside class="project-panel" aria-label="프로젝트 목록">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">WORKSPACE</span>
                    <h1>프로젝트</h1>
                </div>
                <span class="project-total">${activeProjectCount}</span>
            </div>

            <nav class="project-list">
                <c:forEach var="projectStatus" items="${projectListStatuses}">
                    <details class="project-group project-group-${projectStatus.code}"
                             data-project-status="${projectStatus.code}" open>
                        <summary class="project-group-heading">
                            <span class="project-group-title">
                                (<c:out value="${projectStatus.label}" />)
                                <span class="project-group-count">${projectCountsByStatus[projectStatus.code]}개</span>
                            </span>
                        </summary>
                        <div class="project-group-items">
                            <c:forEach var="project" items="${projects}">
                                <c:if test="${project.status == projectStatus.code}">
                                    <c:url var="projectUrl" value="/">
                                        <c:param name="projectId" value="${project.id}" />
                                        <c:param name="view" value="${listMode}" />
                                        <c:param name="sort" value="${sort}" />
                                        <c:param name="direction" value="${direction}" />
                                    </c:url>
                                    <a class="project-item ${project.id == selectedProjectId ? 'is-selected' : ''}"
                                       href="${projectUrl}" data-project-name="${fn:escapeXml(project.name)}"
                                       ${project.id == selectedProjectId ? 'aria-current="page"' : ''}>
                                        <span class="project-icon" aria-hidden="true"><c:out value="${fn:substring(project.name, 0, 1)}" /></span>
                                        <span class="project-copy">
                                            <strong><c:out value="${project.name}" /></strong>
                                            <small>미종료 오류 ${project.openIssueCount}건</small>
                                        </span>
                                        <span class="project-count">${project.openIssueCount}</span>
                                    </a>
                                </c:if>
                            </c:forEach>
                        </div>
                    </details>
                </c:forEach>
            </nav>
            <p id="projectSearchEmpty" class="project-search-empty" hidden>검색 결과가 없습니다.</p>

            <c:if test="${empty projects}">
                <div class="project-empty">
                    <span aria-hidden="true">＋</span>
                    <p>등록된 프로젝트가 없습니다.</p>
                </div>
            </c:if>
        </aside>

        <section class="issue-panel" aria-label="오류 목록">
            <c:if test="${param.projectArchiveError == 'hasOpenIssues'}">
                <div class="page-alert" role="alert">
                    종료되지 않은 오류가 있는 프로젝트는 보관할 수 없습니다.
                </div>
            </c:if>
            <c:if test="${param.issueCreateError == 'duplicate'}">
                <div class="page-alert page-alert-info" role="status">
                    이미 처리된 등록 요청입니다. 오류가 중복으로 등록되지 않았습니다.
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty selectedProject}">
                    <div class="issue-heading">
                        <div>
                            <p class="breadcrumb">프로젝트 / 오류 관리</p>
                            <h2><c:out value="${selectedProject.name}" /></h2>
                            <p class="heading-description">발견된 오류의 처리 상태와 담당자를 확인하세요.</p>
                        </div>
                        <div class="issue-actions" aria-label="선택한 프로젝트 작업">
                            <button type="button" class="action-button"
                                    onclick="document.getElementById('editProjectDialog').showModal()">프로젝트 편집</button>
                            <a class="action-button issue-create-button"
                               href="<c:url value='/projects/${selectedProject.id}/issues/new' />">
                                <span aria-hidden="true">＋</span> 오류 추가
                            </a>
                        </div>
                    </div>

                    <div class="issue-toolbar">
                        <div class="tabs" role="tablist" aria-label="오류 상태 구분">
                            <c:url var="activeUrl" value="/">
                                <c:param name="projectId" value="${selectedProjectId}" />
                                <c:param name="view" value="active" />
                                <c:param name="sort" value="${sort}" />
                                <c:param name="direction" value="${direction}" />
                            </c:url>
                            <c:url var="closedUrl" value="/">
                                <c:param name="projectId" value="${selectedProjectId}" />
                                <c:param name="view" value="closed" />
                                <c:param name="sort" value="${sort}" />
                                <c:param name="direction" value="${direction}" />
                            </c:url>
                            <a href="${activeUrl}" class="tab ${listMode == 'active' ? 'is-active' : ''}" role="tab"
                               aria-selected="${listMode == 'active'}">진행 중</a>
                            <a href="${closedUrl}" class="tab ${listMode == 'closed' ? 'is-active' : ''}" role="tab"
                               aria-selected="${listMode == 'closed'}">종료됨</a>
                        </div>
                        <div class="issue-toolbar-meta">
                            <label class="search-box issue-search-box">
                                <span class="search-icon" aria-hidden="true">⌕</span>
                                <input id="issueSearchInput" class="search-input" type="search"
                                       placeholder="오류 검색" autocomplete="off" aria-label="오류 검색">
                            </label>
                            <div class="sort-controls" aria-label="오류 목록 정렬">
                                <c:url var="severitySortUrl" value="/">
                                    <c:param name="projectId" value="${selectedProjectId}" />
                                    <c:param name="view" value="${listMode}" />
                                    <c:param name="sort" value="severity" />
                                    <c:param name="direction" value="${sort == 'severity' && direction == 'desc' ? 'asc' : 'desc'}" />
                                </c:url>
                                <c:url var="createdAtSortUrl" value="/">
                                    <c:param name="projectId" value="${selectedProjectId}" />
                                    <c:param name="view" value="${listMode}" />
                                    <c:param name="sort" value="createdAt" />
                                    <c:param name="direction" value="${sort == 'createdAt' && direction == 'desc' ? 'asc' : 'desc'}" />
                                </c:url>
                                <a class="sort-button ${sort == 'severity' ? 'is-active' : ''}" href="${severitySortUrl}">
                                    심각도 <span aria-hidden="true">${sort == 'severity' ? (direction == 'desc' ? '↓' : '↑') : '↕'}</span>
                                </a>
                                <a class="sort-button ${sort == 'createdAt' ? 'is-active' : ''}" href="${createdAtSortUrl}">
                                    추가 날짜 <span aria-hidden="true">${sort == 'createdAt' ? (direction == 'desc' ? '↓' : '↑') : '↕'}</span>
                                </a>
                            </div>
                            <span id="issueResultCount" class="result-count">총 ${fn:length(issues)}건</span>
                        </div>
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
                                    <th>등록일</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="issue" items="${issues}">
                                    <tr class="issue-row" data-search-text="${fn:escapeXml(issue.searchText)}">
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
                                        <td class="updated-at"><c:out value="${issue.createdAtDisplay}" /></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>

                        <div id="issueSearchEmpty" class="issue-search-empty" hidden>
                            검색어와 일치하는 오류가 없습니다.
                        </div>

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

    <dialog id="createProjectDialog" class="project-dialog">
        <form action="<c:url value='/projects' />" method="post">
            <div class="dialog-heading">
                <div>
                    <span class="eyebrow">NEW PROJECT</span>
                    <h2>프로젝트 생성</h2>
                </div>
                <button type="button" class="dialog-close"
                        onclick="document.getElementById('createProjectDialog').close()" aria-label="닫기">×</button>
            </div>
            <label class="field-label" for="createProjectName">프로젝트명</label>
            <input id="createProjectName" class="text-input" type="text" name="name"
                   maxlength="200" required autocomplete="off" placeholder="프로젝트명을 입력하세요">
            <label class="field-label field-label-spaced" for="createProjectStatus">프로젝트 상태</label>
            <select id="createProjectStatus" class="text-input" name="status">
                <c:forEach var="statusOption" items="${projectStatusOptions}">
                    <option value="${statusOption.code}">
                        <c:out value="${statusOption.label}" />
                    </option>
                </c:forEach>
            </select>
            <div class="dialog-actions">
                <button type="button" class="action-button"
                        onclick="document.getElementById('createProjectDialog').close()">취소</button>
                <button type="submit" class="action-button action-button-primary">생성</button>
            </div>
        </form>
    </dialog>

    <c:if test="${not empty selectedProject}">
        <dialog id="editProjectDialog" class="project-dialog">
            <div class="dialog-body">
                <div class="dialog-heading">
                    <div>
                        <span class="eyebrow">EDIT PROJECT</span>
                        <h2>프로젝트 수정</h2>
                    </div>
                    <button type="button" class="dialog-close"
                        onclick="document.getElementById('editProjectDialog').close()" aria-label="닫기">×</button>
                </div>
                <form action="<c:url value='/projects/${selectedProject.id}/edit' />" method="post">
                    <label class="field-label" for="editProjectName">프로젝트명</label>
                    <input id="editProjectName" class="text-input" type="text" name="name"
                           value="${fn:escapeXml(selectedProject.name)}" maxlength="200" required autocomplete="off">
                    <label class="field-label field-label-spaced" for="editProjectStatus">프로젝트 상태</label>
                    <select id="editProjectStatus" class="text-input" name="status">
                        <c:if test="${selectedProject.status == 'archived'}">
                            <option value="" selected>보관 상태 유지</option>
                        </c:if>
                        <c:forEach var="statusOption" items="${projectStatusOptions}">
                            <option value="${statusOption.code}"
                                    ${statusOption.code == selectedProject.status ? 'selected' : ''}>
                                <c:out value="${statusOption.label}" />
                            </option>
                        </c:forEach>
                    </select>
                    <div class="dialog-actions">
                        <button type="button" class="action-button"
                                onclick="document.getElementById('editProjectDialog').close()">취소</button>
                        <button type="submit" class="action-button action-button-primary">저장</button>
                    </div>
                </form>
                <div class="danger-zone">
                    <div>
                        <strong>프로젝트 보관</strong>
                        <p>모든 오류가 종료된 프로젝트만 보관할 수 있습니다.</p>
                    </div>
                    <form action="<c:url value='/projects/${selectedProject.id}/archive' />" method="post"
                          onsubmit="return confirm('이 프로젝트를 보관하시겠습니까?');">
                        <button type="submit" class="action-button action-button-danger"
                                ${selectedProject.status == 'archived' ? 'disabled' : ''}>보관</button>
                    </form>
                </div>
            </div>
        </dialog>
    </c:if>
    <script>
        (function () {
            var storageKey = 'qalog.projectGroups.open.v1';
            var groups = document.querySelectorAll('[data-project-status]');
            var savedState = {};
            var ready = false;

            try {
                savedState = JSON.parse(localStorage.getItem(storageKey) || '{}');
            } catch (error) {
                savedState = {};
            }

            Array.prototype.forEach.call(groups, function (group) {
                var status = group.getAttribute('data-project-status');
                if (Object.prototype.hasOwnProperty.call(savedState, status)) {
                    group.open = savedState[status];
                }

                group.addEventListener('toggle', function () {
                    if (!ready) {
                        return;
                    }

                    var currentState = {};
                    Array.prototype.forEach.call(groups, function (currentGroup) {
                        currentState[currentGroup.getAttribute('data-project-status')] = currentGroup.open;
                    });
                    try {
                        localStorage.setItem(storageKey, JSON.stringify(currentState));
                    } catch (error) {
                        // 저장 공간을 사용할 수 없어도 목록 펼침 기능은 그대로 사용한다.
                    }
                });
            });

            window.requestAnimationFrame(function () {
                ready = true;
            });

            var projectSearchInput = document.getElementById('projectSearchInput');
            var projectSearchEmpty = document.getElementById('projectSearchEmpty');
            if (projectSearchInput) {
                projectSearchInput.addEventListener('input', function () {
                    var keyword = projectSearchInput.value.trim().toLowerCase();
                    var visibleProjectCount = 0;

                    Array.prototype.forEach.call(groups, function (group) {
                        var visibleInGroup = 0;
                        var projectItems = group.querySelectorAll('[data-project-name]');
                        Array.prototype.forEach.call(projectItems, function (item) {
                            var projectName = item.getAttribute('data-project-name').toLowerCase();
                            var matches = projectName.indexOf(keyword) !== -1;
                            item.hidden = !matches;
                            if (matches) {
                                visibleInGroup++;
                                visibleProjectCount++;
                            }
                        });
                        group.hidden = visibleInGroup === 0;
                    });

                    projectSearchEmpty.hidden = visibleProjectCount !== 0;
                });
            }

            var issueSearchInput = document.getElementById('issueSearchInput');
            var issueRows = document.querySelectorAll('.issue-row');
            var issueResultCount = document.getElementById('issueResultCount');
            var issueSearchEmpty = document.getElementById('issueSearchEmpty');
            if (issueSearchInput) {
                issueSearchInput.addEventListener('input', function () {
                    var keyword = issueSearchInput.value.trim().toLowerCase();
                    var visibleIssueCount = 0;

                    Array.prototype.forEach.call(issueRows, function (row) {
                        var searchText = row.getAttribute('data-search-text').toLowerCase();
                        var matches = searchText.indexOf(keyword) !== -1;
                        row.hidden = !matches;
                        if (matches) {
                            visibleIssueCount++;
                        }
                    });

                    issueResultCount.textContent = '총 ' + visibleIssueCount + '건';
                    issueSearchEmpty.hidden = keyword === '' || visibleIssueCount !== 0 || issueRows.length === 0;
                });
            }
        }());
    </script>
</body>
</html>
