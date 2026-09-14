<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%-- EL 문자열 리터럴 '\n'은 이스케이프로 해석되지 않고 그대로 백슬래시+n 두 글자가 되므로,
     재현 순서 줄바꿈 분리에 쓸 진짜 개행 문자는 스크립틀릿으로 만들어 pageContext에 넣어둔다. --%>
<% pageContext.setAttribute("NEWLINE_CHARS", "\r\n"); %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>#${issue.issueNumber} ${empty issue.title ? '제목 없음' : fn:escapeXml(issue.title)} · QA로그</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap">
<style>
  :root {
    --bg:#F7F8FA; --surface:#FFFFFF; --surface-2:#F4F5F7;
    --ink:#171A1F; --ink-muted:#565C66; --ink-faint:#8D939C;
    --border:#DFE2E7; --border-strong:#C7CBD3;
    --accent:#2F6FED; --accent-ink:#FFFFFF; --accent-soft:#EAF1FE;
    --danger:#D3374A; --danger-soft:#FCEAEC;
    --shadow:0 1px 2px rgba(31,35,40,.04), 0 8px 20px rgba(31,35,40,.05);

    --sev-unspecified-bg:#EEF0F5; --sev-unspecified-fg:#5B6273;
    --sev-low-bg:#FBE7E9;         --sev-low-fg:#B0454E;
    --sev-medium-bg:#F7C9CD;      --sev-medium-fg:#9C2731;
    --sev-high-bg:#EE97A0;        --sev-high-fg:#7A0F1C;
    --sev-critical-bg:#B0182B;    --sev-critical-fg:#FFFFFF;

    --pri-unspecified-bg:#EEF0F5; --pri-unspecified-fg:#5B6273;
    --pri-low-bg:#FBEBD2;         --pri-low-fg:#966423;
    --pri-normal-bg:#F6D89B;      --pri-normal-fg:#8A5A0F;
    --pri-high-bg:#EFAF56;        --pri-high-fg:#6E3D06;
    --pri-urgent-bg:#C2410C;      --pri-urgent-fg:#FFFFFF;

    --status-new:#3B82F6; --status-reviewing:#7C6FE0; --status-fixing:#D97706; --status-closed:#16A34A;
  }
  * { box-sizing:border-box; }
  body { margin:0; background:var(--bg); color:var(--ink); font-family:"Noto Sans KR","Malgun Gothic",sans-serif; font-size:14px; line-height:1.6; }
  .mono { font-family:"JetBrains Mono","Consolas",monospace; }
  a { color:var(--accent); }

  .topbar { display:flex; align-items:center; justify-content:space-between; height:52px; padding:0 24px; background:var(--surface); border-bottom:1px solid var(--border); }
  .brand { font-weight:700; }
  .back-link { font-size:13px; color:var(--ink-muted); text-decoration:none; }

  .detail { max-width:920px; margin:0 auto; padding:28px 24px 80px; }

  .conflict-banner { background:var(--danger-soft); color:var(--danger); border:1px solid var(--danger); border-radius:8px; padding:10px 14px; margin-bottom:16px; font-size:13px; }

  .crumb { font-size:12.5px; color:var(--ink-faint); margin-bottom:10px; }
  .crumb .sep { margin:0 6px; }

  .badge-row { display:flex; align-items:flex-end; gap:22px; margin-bottom:16px; flex-wrap:wrap; }
  .badge-group { display:flex; flex-direction:column; gap:5px; }
  .badge-label { font-size:10.5px; letter-spacing:.06em; text-transform:uppercase; color:var(--ink-faint); font-weight:600; }
  .chip { display:inline-flex; align-items:center; gap:6px; padding:7px 16px; border-radius:8px; font-size:15px; font-weight:700; width:fit-content; }
  .status-chip { display:inline-flex; align-items:center; gap:7px; padding:5px 12px; border-radius:999px; font-size:12.5px; font-weight:600; background:var(--surface-2); border:1px solid var(--border-strong); color:var(--ink); width:fit-content; }
  .status-dot { width:8px; height:8px; border-radius:50%; flex:none; }

  h1.title { font-size:24px; font-weight:700; margin:0 0 4px; }

  .meta-bar { display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:16px; background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:16px 20px; margin:20px 0; box-shadow:var(--shadow); }
  .meta-facts { display:flex; align-items:center; gap:26px; flex-wrap:wrap; }
  .meta-item { display:flex; flex-direction:column; gap:3px; }
  .meta-item .k { font-size:10.5px; color:var(--ink-faint); text-transform:uppercase; letter-spacing:.05em; }
  .meta-item .v { font-size:13.5px; color:var(--ink); font-weight:600; }
  .meta-item .v .mono { color:var(--ink-faint); font-weight:500; }
  .assignee-form { display:flex; align-items:center; gap:8px; background:var(--accent-soft); border-radius:10px; padding:6px 10px; }
  .meta-actions { display:flex; gap:8px; flex-wrap:wrap; align-items:center; }

  select, input[type=text] { font-family:inherit; font-size:12.5px; padding:6px 8px; border-radius:6px; border:1px solid var(--border-strong); background:var(--surface); color:var(--ink); }
  .btn { font-family:inherit; font-size:12.5px; font-weight:600; padding:8px 13px; border-radius:8px; border:1px solid var(--border-strong); background:var(--surface); color:var(--ink); cursor:pointer; }
  .btn:hover { border-color:var(--accent); color:var(--accent); }
  .btn-primary { background:var(--accent); border-color:var(--accent); color:var(--accent-ink); }
  .btn-danger { border-color:var(--danger); color:var(--danger); background:transparent; }
  .btn-danger:hover { background:var(--danger-soft); }
  form.inline { display:inline-flex; align-items:center; gap:6px; }

  .panel { background:var(--surface); border:1px solid var(--border); border-radius:12px; box-shadow:var(--shadow); margin-bottom:20px; overflow:hidden; }
  .panel-title { font-size:12px; font-weight:700; color:var(--ink-faint); text-transform:uppercase; letter-spacing:.06em; padding:16px 20px 0; }
  .field-list { display:flex; flex-direction:column; margin-top:10px; }
  .field-row { display:grid; grid-template-columns:150px 1fr; padding:13px 20px; border-top:1px solid var(--border); }
  .field-row:first-of-type { border-top:none; }
  .field-row:nth-of-type(even) { background:var(--surface-2); }
  .field-row .k { font-size:12.5px; color:var(--ink-faint); font-weight:500; padding:1px 18px 0 0; border-right:1px solid var(--border); }
  .field-row .v { font-size:13.5px; color:var(--ink); font-weight:500; padding-left:18px; }
  .field-row .v ol { margin:0; padding-left:18px; }
  .field-row .v ol li { margin-bottom:3px; }
  .field-row.suggested .v { background:var(--accent-soft); border-radius:8px; padding:10px 12px; }
  .empty { color:var(--ink-faint); }

  .attachments { padding:16px 20px 20px; display:flex; gap:12px; flex-wrap:wrap; }
  .thumb { width:132px; border:1px solid var(--border); border-radius:10px; overflow:hidden; background:var(--surface-2); text-decoration:none; color:inherit; display:block; }
  .thumb img { width:100%; height:88px; object-fit:cover; display:block; border-bottom:1px solid var(--border); }
  .thumb-cap { padding:7px 9px; }
  .thumb-cap .name { font-size:11.5px; color:var(--ink); font-weight:600; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
  .thumb-cap .size { font-size:10.5px; color:var(--ink-faint); font-family:"JetBrains Mono",monospace; }
  .thumb-inline { width:220px; margin-top:8px; }
  .thumb-inline img { height:130px; }

  .tabs-nav { display:flex; gap:4px; padding:14px 20px 0; }
  .tab-btn { font-family:inherit; font-size:13px; font-weight:600; padding:9px 14px; background:none; border:none; border-bottom:2px solid transparent; color:var(--ink-faint); cursor:pointer; }
  .tab-btn.active { color:var(--accent); border-bottom-color:var(--accent); }
  .tab-panel { padding:6px 20px 20px; }
  .tab-panel[hidden] { display:none; }

  .log-list { display:flex; flex-direction:column; }
  .log-row { display:grid; grid-template-columns:18px 1fr auto; gap:10px; align-items:start; padding:9px 0; position:relative; }
  .log-row::before { content:""; position:absolute; left:8px; top:22px; bottom:-9px; width:1px; background:var(--border); }
  .log-row:last-child::before { display:none; }
  .log-dot { width:9px; height:9px; border-radius:50%; background:var(--border-strong); margin-top:5px; }
  .log-text { font-size:13px; color:var(--ink-muted); }
  .log-text b { color:var(--ink); font-weight:600; }
  .log-time { font-family:"JetBrains Mono",monospace; font-size:11.5px; color:var(--ink-faint); white-space:nowrap; }
  .log-toggle { font-family:inherit; font-size:12px; color:var(--accent); background:none; border:none; padding:0 0 0 6px; cursor:pointer; }
  .log-toggle:hover { text-decoration:underline; }
  .log-group-items { margin-top:8px; padding-left:14px; border-left:2px solid var(--border); display:flex; flex-direction:column; gap:6px; }
  .log-subitem { display:flex; justify-content:space-between; gap:10px; font-size:12.5px; color:var(--ink-muted); }
  .log-subitem .log-text { color:var(--ink-muted); }
  .log-subitem .log-text b { color:var(--ink); }

  .comment-list { display:flex; flex-direction:column; gap:12px; margin-bottom:16px; }
  .comment-card { display:flex; gap:10px; }
  .avatar { width:26px; height:26px; border-radius:50%; background:var(--accent-soft); color:var(--accent); display:inline-flex; align-items:center; justify-content:center; font-size:11px; font-weight:700; flex:none; }
  .comment-head { display:flex; align-items:baseline; gap:8px; margin-bottom:3px; }
  .comment-author { font-size:13px; font-weight:700; }
  .comment-time { font-family:"JetBrains Mono",monospace; font-size:11px; color:var(--ink-faint); }
  .comment-text { font-size:13.5px; color:var(--ink); background:var(--surface-2); border:1px solid var(--border); border-radius:0 10px 10px 10px; padding:10px 13px; white-space:pre-line; }
  .composer { display:flex; gap:10px; border-top:1px solid var(--border); padding-top:16px; }
  .composer textarea { flex:1; resize:vertical; min-height:56px; font-family:inherit; font-size:13.5px; padding:10px 12px; border:1px solid var(--border); border-radius:10px; background:var(--surface-2); color:var(--ink); }
</style>
</head>
<body>
  <header class="topbar">
    <span class="brand">QA로그</span>
    <a class="back-link" href="${ctx}/">← 목록으로</a>
  </header>

  <main class="detail">
    <c:if test="${conflict}">
      <div class="conflict-banner">다른 사용자가 먼저 수정했습니다. 아래 내용을 새로고침한 뒤 다시 시도하세요.</div>
    </c:if>
    <c:if test="${forbidden}">
      <div class="conflict-banner">이 작업은 오류 등록자 또는 처리 담당자만 할 수 있습니다.</div>
    </c:if>

    <div class="crumb">${fn:escapeXml(issue.projectName)} <span class="sep">/</span> <span class="mono">#${issue.issueNumber}</span></div>

    <div class="badge-row">
      <div class="badge-group">
        <span class="badge-label">심각도</span>
        <c:choose>
          <c:when test="${issue.severity == 'low'}"><span class="chip" style="background:var(--sev-low-bg);color:var(--sev-low-fg);">낮음</span></c:when>
          <c:when test="${issue.severity == 'medium'}"><span class="chip" style="background:var(--sev-medium-bg);color:var(--sev-medium-fg);">보통</span></c:when>
          <c:when test="${issue.severity == 'high'}"><span class="chip" style="background:var(--sev-high-bg);color:var(--sev-high-fg);">높음</span></c:when>
          <c:when test="${issue.severity == 'critical'}"><span class="chip" style="background:var(--sev-critical-bg);color:var(--sev-critical-fg);">치명적</span></c:when>
          <c:otherwise><span class="chip" style="background:var(--sev-unspecified-bg);color:var(--sev-unspecified-fg);">미지정</span></c:otherwise>
        </c:choose>
      </div>
      <div class="badge-group">
        <span class="badge-label">우선순위</span>
        <c:choose>
          <c:when test="${issue.priority == 'low'}"><span class="chip" style="background:var(--pri-low-bg);color:var(--pri-low-fg);">낮음</span></c:when>
          <c:when test="${issue.priority == 'normal'}"><span class="chip" style="background:var(--pri-normal-bg);color:var(--pri-normal-fg);">보통</span></c:when>
          <c:when test="${issue.priority == 'high'}"><span class="chip" style="background:var(--pri-high-bg);color:var(--pri-high-fg);">높음</span></c:when>
          <c:when test="${issue.priority == 'urgent'}"><span class="chip" style="background:var(--pri-urgent-bg);color:var(--pri-urgent-fg);">긴급</span></c:when>
          <c:otherwise><span class="chip" style="background:var(--pri-unspecified-bg);color:var(--pri-unspecified-fg);">미지정</span></c:otherwise>
        </c:choose>
      </div>
      <div class="badge-group">
        <span class="badge-label">상태</span>
        <c:choose>
          <c:when test="${issue.status == 'reviewing'}"><span class="status-chip"><span class="status-dot" style="background:var(--status-reviewing);"></span>확인 중</span></c:when>
          <c:when test="${issue.status == 'fixing'}"><span class="status-chip"><span class="status-dot" style="background:var(--status-fixing);"></span>수정 중</span></c:when>
          <c:when test="${issue.status == 'closed'}"><span class="status-chip"><span class="status-dot" style="background:var(--status-closed);"></span>종료</span></c:when>
          <c:otherwise><span class="status-chip"><span class="status-dot" style="background:var(--status-new);"></span>신규</span></c:otherwise>
        </c:choose>
      </div>
    </div>

    <h1 class="title">${empty issue.title ? '제목 없음' : fn:escapeXml(issue.title)}</h1>

    <div class="meta-bar">
      <div class="meta-facts">
        <div class="meta-item">
          <span class="k">등록자</span>
          <span class="v">${fn:escapeXml(issue.createdByName)} · <span class="mono">${issue.createdAtDisplay}</span></span>
        </div>
        <c:choose>
          <c:when test="${canManage}">
            <form class="assignee-form" action="${ctx}/issues/${issue.id}/assignee" method="post">
              <div class="meta-item">
                <span class="k">현재 처리 담당자</span>
                <select name="assigneeId">
                  <option value="">미지정</option>
                  <c:forEach var="u" items="${users}">
                    <option value="${u.id}" ${u.id == issue.assigneeId ? 'selected' : ''}>${fn:escapeXml(u.displayName)}</option>
                  </c:forEach>
                </select>
              </div>
              <!-- expectedUpdatedAt은 낙관적 잠금 대조용 원본 정밀도 값 - 화면표시(createdAtDisplay 등)와
                   달리 절대 KoreanDateTime으로 가공하면 안 된다. -->
              <input type="hidden" name="expectedUpdatedAt" value="${issue.updatedAt}">
              <button type="submit" class="btn">변경</button>
            </form>
          </c:when>
          <c:when test="${canClaimAssignee}">
            <%-- 담당자 미지정 상태의 예외 - 등록자/담당자가 아니어도 자기 자신만 담당자로 지정 가능
                 (등록자·담당자가 둘 다 자리를 비워 오류가 영원히 미지정으로 남는 것을 막기 위함) --%>
            <form class="assignee-form" action="${ctx}/issues/${issue.id}/assignee" method="post">
              <div class="meta-item">
                <span class="k">현재 처리 담당자</span>
                <select name="assigneeId">
                  <option value="" selected>미지정</option>
                  <option value="${sessionScope.LOGIN_USER_ID}">${fn:escapeXml(sessionScope.loginDisplayName)}(나)</option>
                </select>
              </div>
              <input type="hidden" name="expectedUpdatedAt" value="${issue.updatedAt}">
              <button type="submit" class="btn">내가 맡기</button>
            </form>
          </c:when>
          <c:otherwise>
            <div class="meta-item">
              <span class="k">현재 처리 담당자</span>
              <span class="v">${empty issue.assigneeName ? '미지정' : fn:escapeXml(issue.assigneeName)}</span>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="meta-item">
          <span class="k">최근 변경</span>
          <span class="v">${fn:escapeXml(issue.updatedByName)} · <span class="mono">${issue.updatedAtDisplay}</span></span>
        </div>
      </div>
      <c:if test="${canManage}">
        <div class="meta-actions">
          <%-- 아래 상태변경/종료/재오픈 폼의 expectedUpdatedAt도 위 담당자 폼과 동일하게 원본 정밀도 값을 그대로 써야 함 --%>
          <c:if test="${issue.status != 'closed'}">
            <form class="inline" action="${ctx}/issues/${issue.id}/status" method="post">
              <select name="status">
                <c:forEach var="opt" items="${statusOptions}">
                  <option value="${opt.code}" ${opt.code == issue.status ? 'selected' : ''}>${opt.label}</option>
                </c:forEach>
              </select>
              <input type="hidden" name="expectedUpdatedAt" value="${issue.updatedAt}">
              <button type="submit" class="btn">상태 변경</button>
            </form>
            <form class="inline" action="${ctx}/issues/${issue.id}/close" method="post">
              <input type="hidden" name="expectedUpdatedAt" value="${issue.updatedAt}">
              <button type="submit" class="btn btn-danger">종료</button>
            </form>
          </c:if>
          <c:if test="${issue.status == 'closed'}">
            <form class="inline" action="${ctx}/issues/${issue.id}/reopen" method="post">
              <input type="hidden" name="expectedUpdatedAt" value="${issue.updatedAt}">
              <button type="submit" class="btn btn-primary">다시 열기</button>
            </form>
          </c:if>
          <a class="btn" href="${ctx}/issues/${issue.id}/edit">수정</a>
        </div>
      </c:if>
    </div>

    <section class="panel">
      <div class="panel-title">오류 내용</div>
      <div class="field-list">
        <div class="field-row">
          <div class="k">발생 위치</div>
          <div class="v ${empty issue.location ? 'empty' : ''}">${empty issue.location ? '—' : fn:escapeXml(issue.location)}</div>
        </div>
        <div class="field-row">
          <div class="k">URL 주소</div>
          <div class="v">
            <c:choose>
              <c:when test="${fn:startsWith(issue.locationUrl, 'http://') or fn:startsWith(issue.locationUrl, 'https://')}">
                <a class="mono" href="${fn:escapeXml(issue.locationUrl)}" target="_blank" rel="noopener noreferrer">${fn:escapeXml(issue.locationUrl)}</a>
              </c:when>
              <c:when test="${not empty issue.locationUrl}">
                <%-- http(s)로 시작하지 않으면 활성 링크로 만들지 않고 일반 텍스트로만 표시(오류등록_기능명세서.md 3절) --%>
                <span class="mono">${fn:escapeXml(issue.locationUrl)}</span>
              </c:when>
              <c:otherwise><span class="empty">—</span></c:otherwise>
            </c:choose>
          </div>
        </div>
        <div class="field-row">
          <div class="k">재현 순서</div>
          <div class="v">
            <c:choose>
              <c:when test="${not empty issue.stepsToReproduce}">
                <ol>
                  <c:forEach var="step" items="${fn:split(issue.stepsToReproduce, NEWLINE_CHARS)}">
                    <c:if test="${not empty fn:trim(step)}"><li>${fn:escapeXml(step)}</li></c:if>
                  </c:forEach>
                </ol>
              </c:when>
              <c:otherwise><span class="empty">—</span></c:otherwise>
            </c:choose>
          </div>
        </div>
        <div class="field-row">
          <div class="k">기대 결과</div>
          <div class="v ${empty issue.expectedResult ? 'empty' : ''}">${empty issue.expectedResult ? '—' : fn:escapeXml(issue.expectedResult)}</div>
        </div>
        <c:forEach var="a" items="${issue.attachments}">
          <c:if test="${a.context == 'expected_result'}">
            <div class="field-row">
              <div class="k"></div>
              <div class="v"><a class="thumb thumb-inline" href="${ctx}/issues/${issue.id}/attachments/${a.id}" target="_blank"><img src="${ctx}/issues/${issue.id}/attachments/${a.id}" alt="${fn:escapeXml(a.originalName)}"></a></div>
            </div>
          </c:if>
        </c:forEach>
        <div class="field-row">
          <div class="k">실제 결과</div>
          <div class="v ${empty issue.actualResult ? 'empty' : ''}">${empty issue.actualResult ? '—' : fn:escapeXml(issue.actualResult)}</div>
        </div>
        <c:forEach var="a" items="${issue.attachments}">
          <c:if test="${a.context == 'actual_result'}">
            <div class="field-row">
              <div class="k"></div>
              <div class="v"><a class="thumb thumb-inline" href="${ctx}/issues/${issue.id}/attachments/${a.id}" target="_blank"><img src="${ctx}/issues/${issue.id}/attachments/${a.id}" alt="${fn:escapeXml(a.originalName)}"></a></div>
            </div>
          </c:if>
        </c:forEach>
        <div class="field-row">
          <div class="k">테스트 버전</div>
          <div class="v mono ${empty issue.testVersion ? 'empty' : ''}">${empty issue.testVersion ? '—' : fn:escapeXml(issue.testVersion)}</div>
        </div>
        <div class="field-row">
          <div class="k">테스트 환경</div>
          <div class="v ${empty issue.testEnvironment ? 'empty' : ''}">${empty issue.testEnvironment ? '—' : fn:escapeXml(issue.testEnvironment)}</div>
        </div>
        <div class="field-row suggested">
          <div class="k">개선 방향</div>
          <div class="v ${empty issue.suggestedFix ? 'empty' : ''}">${empty issue.suggestedFix ? '—' : fn:escapeXml(issue.suggestedFix)}</div>
        </div>
      </div>
      <%-- context가 없는(일반) 첨부만 여기 그리드에 표시 - 기대/실제 결과 전용 첨부는 위에서 각 필드 바로 아래 표시됨 --%>
      <c:set var="hasGeneralAttachment" value="false" />
      <c:forEach var="a" items="${issue.attachments}"><c:if test="${empty a.context}"><c:set var="hasGeneralAttachment" value="true" /></c:if></c:forEach>
      <c:if test="${hasGeneralAttachment}">
        <div class="attachments">
          <c:forEach var="a" items="${issue.attachments}">
            <c:if test="${empty a.context}">
              <a class="thumb" href="${ctx}/issues/${issue.id}/attachments/${a.id}" target="_blank">
                <img src="${ctx}/issues/${issue.id}/attachments/${a.id}" alt="${fn:escapeXml(a.originalName)}">
                <div class="thumb-cap">
                  <div class="name">${fn:escapeXml(a.originalName)}</div>
                  <div class="size"><fmt:formatNumber value="${a.sizeBytes / 1024}" maxFractionDigits="0"/> KB</div>
                </div>
              </a>
            </c:if>
          </c:forEach>
        </div>
      </c:if>
    </section>

    <section class="panel">
      <div class="tabs-nav">
        <button class="tab-btn" id="tabBtnHistory" onclick="showTab('history')">변경 이력 <span class="mono">${histories.size()}</span></button>
        <button class="tab-btn active" id="tabBtnComments" onclick="showTab('comments')">댓글 <span class="mono">${comments.size()}</span></button>
      </div>

      <div class="tab-panel" id="panelHistory" hidden>
        <div class="log-list">
          <c:forEach var="g" items="${historyGroups}">
            <c:choose>
              <%-- 한 번의 저장 요청으로 항목이 하나뿐이면 지금까지처럼 그 문장을 그대로 보여준다. --%>
              <c:when test="${g.count == 1}">
                <c:set var="h" value="${g.single}" />
                <div class="log-row">
                  <span class="log-dot"></span>
                  <span class="log-text">
                    <c:choose>
                      <c:when test="${h.eventType == 'created'}"><b>${fn:escapeXml(h.actorName)}</b>님이 오류를 등록했습니다</c:when>
                      <c:when test="${h.eventType == 'attachment_added'}"><b>${fn:escapeXml(h.actorName)}</b>님이 스크린샷을 첨부했습니다</c:when>
                      <c:when test="${h.eventType == 'attachment_removed'}"><b>${fn:escapeXml(h.actorName)}</b>님이 첨부파일을 삭제했습니다</c:when>
                      <c:when test="${h.eventType == 'field_changed'}"><b>${fn:escapeXml(h.actorName)}</b>님이 ${fn:escapeXml(h.fieldLabel)}${h.fieldJosaEul} ${fn:escapeXml(h.oldValueDisplay)} → ${fn:escapeXml(h.newValueDisplay)}${h.newValueJosaRo} 변경</c:when>
                      <c:otherwise>${fn:escapeXml(h.eventType)}</c:otherwise>
                    </c:choose>
                  </span>
                  <span class="log-time mono">${h.createdAtDisplay}</span>
                </div>
              </c:when>
              <%-- 항목이 여러 개면 "변경 N건" 요약 한 줄 + 기본 접힌 펼치기 목록으로 묶는다
                   (등록/수정 시 필드 여러 개 + 첨부 추가/삭제가 한꺼번에 남는 걸 깔끔하게 보여주기 위함,
                   2026-09-14 팀 결정). 세부 항목의 문구 조립 규칙은 위 단일 항목 분기와 동일하다. --%>
              <c:otherwise>
                <div class="log-row">
                  <span class="log-dot"></span>
                  <span class="log-text">
                    <b>${fn:escapeXml(g.actorName)}</b>님이 오류를 수정했습니다
                    <button type="button" class="log-toggle" data-label="변경 ${g.count}건 보기" onclick="toggleLogGroup(this)">변경 ${g.count}건 보기</button>
                    <div class="log-group-items" hidden>
                      <c:forEach var="h" items="${g.items}">
                        <div class="log-subitem">
                          <span class="log-text">
                            <c:choose>
                              <c:when test="${h.eventType == 'attachment_added'}"><b>${fn:escapeXml(h.actorName)}</b>님이 스크린샷을 첨부했습니다</c:when>
                              <c:when test="${h.eventType == 'attachment_removed'}"><b>${fn:escapeXml(h.actorName)}</b>님이 첨부파일을 삭제했습니다</c:when>
                              <c:when test="${h.eventType == 'field_changed'}"><b>${fn:escapeXml(h.actorName)}</b>님이 ${fn:escapeXml(h.fieldLabel)}${h.fieldJosaEul} ${fn:escapeXml(h.oldValueDisplay)} → ${fn:escapeXml(h.newValueDisplay)}${h.newValueJosaRo} 변경</c:when>
                              <c:otherwise>${fn:escapeXml(h.eventType)}</c:otherwise>
                            </c:choose>
                          </span>
                          <span class="log-time mono">${h.createdAtDisplay}</span>
                        </div>
                      </c:forEach>
                    </div>
                  </span>
                  <span class="log-time mono">${g.createdAtDisplay}</span>
                </div>
              </c:otherwise>
            </c:choose>
          </c:forEach>
          <c:if test="${empty historyGroups}"><p class="empty">아직 이력이 없습니다.</p></c:if>
        </div>
      </div>

      <div class="tab-panel" id="panelComments">
        <div class="comment-list">
          <c:forEach var="cm" items="${comments}">
            <div class="comment-card">
              <span class="avatar">${fn:escapeXml(fn:substring(cm.authorName, 0, 2))}</span>
              <div>
                <div class="comment-head">
                  <span class="comment-author">${fn:escapeXml(cm.authorName)}</span>
                  <span class="comment-time mono">${cm.createdAtDisplay}</span>
                </div>
                <div class="comment-text">${fn:escapeXml(cm.content)}</div>
              </div>
            </div>
          </c:forEach>
          <c:if test="${empty comments}"><p class="empty">아직 댓글이 없습니다.</p></c:if>
        </div>
        <form class="composer" action="${ctx}/issues/${issue.id}/comments" method="post">
          <textarea name="content" placeholder="댓글을 입력하세요" required></textarea>
          <button type="submit" class="btn btn-primary">댓글 등록</button>
        </form>
      </div>
    </section>
  </main>

  <script>
    function showTab(name) {
      var isHistory = name === 'history';
      document.getElementById('panelHistory').hidden = !isHistory;
      document.getElementById('panelComments').hidden = isHistory;
      document.getElementById('tabBtnHistory').classList.toggle('active', isHistory);
      document.getElementById('tabBtnComments').classList.toggle('active', !isHistory);
    }

    /** 변경 이력에서 "변경 N건 보기" 클릭 시 세부 항목을 펼치고/접는다(기본은 접힌 상태). */
    function toggleLogGroup(button) {
      var items = button.nextElementSibling;
      var willShow = items.hidden;
      items.hidden = !willShow;
      button.textContent = willShow ? '접기' : button.dataset.label;
    }
  </script>
</body>
</html>
