<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>오류 등록 · ${project.name} · QA로그</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap">
<link rel="stylesheet" href="<c:url value='/resources/css/main.css' />">
<style>
  :root {
    --bg:#F7F8FA; --surface:#FFFFFF; --surface-2:#F4F5F7;
    --ink:#171A1F; --ink-muted:#565C66; --ink-faint:#8D939C;
    --border:#DFE2E7; --border-strong:#C7CBD3;
    --accent:#2F6FED; --accent-ink:#FFFFFF; --accent-soft:#EAF1FE;
    --danger:#D3374A;
  }
  * { box-sizing:border-box; }
  body { margin:0; background:var(--bg); color:var(--ink); font-family:"Noto Sans KR","Malgun Gothic",sans-serif; font-size:14px; line-height:1.6; }
  .mono { font-family:"JetBrains Mono","Consolas",monospace; }

  .topbar { display:flex; align-items:center; justify-content:space-between; height:52px; padding:0 24px; background:var(--surface); border-bottom:1px solid var(--border); }
  .brand { font-weight:700; }
  .back-link { font-size:13px; color:var(--ink-muted); text-decoration:none; }

  .register-page { max-width:860px; margin:0 auto; padding:0 0 80px; }
  .crumb { font-size:12.5px; color:var(--ink-faint); margin-bottom:6px; }
  h1.title { font-size:22px; font-weight:700; margin:0 0 20px; }

  .panel { background:var(--surface); border:1px solid var(--border); border-radius:12px; box-shadow:0 1px 2px rgba(31,35,40,.04), 0 8px 20px rgba(31,35,40,.05); padding:8px 24px 24px; }

  .field { display:flex; flex-direction:column; gap:6px; padding:14px 0; border-top:1px solid var(--border); }
  .field:first-of-type { border-top:none; padding-top:0; }
  .field label { font-size:12.5px; font-weight:600; color:var(--ink-muted); }
  .hint { font-size:11.5px; color:var(--ink-faint); }

  input[type=text], textarea, select {
    font-family:inherit; font-size:13.5px; padding:9px 11px; border-radius:8px;
    border:1px solid var(--border-strong); background:var(--surface-2); color:var(--ink); width:100%;
  }
  textarea { resize:vertical; min-height:70px; }
  input:focus, textarea:focus, select:focus { outline:2px solid var(--accent); outline-offset:1px; }

  .row2 { display:grid; grid-template-columns:1fr 1fr; gap:16px; }

  .dropzone {
    border:1px dashed var(--border-strong); border-radius:10px; padding:22px; text-align:center;
    color:var(--ink-faint); font-size:13px; cursor:pointer; background:var(--surface-2);
  }
  .dropzone.dragover { border-color:var(--accent); color:var(--accent); background:var(--accent-soft); }
  .dropzone-sm { padding:10px; font-size:12px; margin-top:2px; }
  .file-list { display:flex; flex-direction:column; gap:6px; margin-top:10px; }
  .file-item {
    display:flex; align-items:center; justify-content:space-between; gap:8px;
    font-size:12.5px; background:var(--surface-2); border:1px solid var(--border); border-radius:8px; padding:7px 10px;
  }
  .file-item .name { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
  .file-item .size { color:var(--ink-faint); font-family:"JetBrains Mono",monospace; flex:none; }
  .file-item .remove { background:none; border:none; color:var(--danger); cursor:pointer; font-size:13px; flex:none; }
  .file-reject { color:var(--danger); font-size:12px; margin-top:6px; }

  .actions { display:flex; justify-content:flex-end; gap:10px; margin-top:22px; }
  .btn { font-family:inherit; font-size:13px; font-weight:600; padding:10px 18px; border-radius:8px; border:1px solid var(--border-strong); background:var(--surface); color:var(--ink); cursor:pointer; text-decoration:none; display:inline-block; }
  .btn:hover { border-color:var(--accent); color:var(--accent); }
  .btn-primary { background:var(--accent); border-color:var(--accent); color:var(--accent-ink); }
  .btn-primary:hover { opacity:.92; color:var(--accent-ink); }
</style>
</head>
<body>
  <header class="app-header">
    <a class="brand" href="${ctx}/">
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
        <c:forEach var="projectItem" items="${projects}">
          <c:url var="projectUrl" value="/">
            <c:param name="projectId" value="${projectItem.id}" />
          </c:url>
          <a class="project-item ${projectItem.id == selectedProjectId ? 'is-selected' : ''}" href="${projectUrl}">
            <span class="project-icon" aria-hidden="true"><c:out value="${fn:substring(projectItem.name, 0, 1)}" /></span>
            <span class="project-copy">
              <strong><c:out value="${projectItem.name}" /></strong>
              <small>미종료 오류 ${projectItem.openIssueCount}건</small>
              <small class="project-assignees">담당자 <c:out value="${empty projectItem.assigneeNames ? '미지정' : projectItem.assigneeNames}" /></small>
            </span>
            <span class="project-count">${projectItem.openIssueCount}</span>
          </a>
        </c:forEach>
      </nav>
    </aside>

    <section class="issue-panel" aria-label="새 오류 등록">
      <div class="register-page">
        <div class="crumb"><c:out value="${project.name}" /> / 오류 관리</div>
        <h1 class="title">새 오류 추가</h1>

        <form class="panel" action="${ctx}/projects/${project.id}/issues" method="post" enctype="multipart/form-data" id="registerForm">
      <input type="hidden" name="registrationToken" value="${registrationToken}">

      <div class="field">
        <label>제목</label>
        <input type="text" name="title" placeholder="제목 (선택)">
      </div>

      <div class="field row2">
        <div>
          <label>심각도</label>
          <select name="severity">
            <c:forEach var="opt" items="${severityOptions}">
              <option value="${opt.code}">${opt.label}</option>
            </c:forEach>
          </select>
        </div>
        <div>
          <label>우선순위</label>
          <select name="priority">
            <c:forEach var="opt" items="${priorityOptions}">
              <option value="${opt.code}">${opt.label}</option>
            </c:forEach>
          </select>
        </div>
      </div>

      <div class="field">
        <label>발생 위치</label>
        <input type="text" name="location" placeholder="화면·기능 (선택)">
      </div>

      <div class="field">
        <label>URL 주소</label>
        <input type="text" name="locationUrl" placeholder="https:// (선택)">
      </div>

      <div class="field row2">
        <div>
          <label>테스트 버전</label>
          <input type="text" name="testVersion" placeholder="예: v1.4.2-rc3">
        </div>
        <div>
          <label>테스트 환경</label>
          <input type="text" name="testEnvironment" placeholder="브라우저·기기·OS">
        </div>
      </div>

      <div class="field">
        <label>재현 순서</label>
        <textarea name="stepsToReproduce" placeholder="한 줄에 하나씩 입력하세요"></textarea>
        <span class="hint">줄바꿈 기준으로 번호 목록으로 표시됩니다</span>
      </div>

      <div class="field">
        <label>기대 결과</label>
        <textarea name="expectedResult"></textarea>
        <div class="dropzone dropzone-sm" id="dropzone-expected">스크린샷 첨부 (선택)</div>
        <input type="file" id="fileInput-expected" name="expectedResultFiles" multiple accept="image/png,image/jpeg,image/gif,image/webp" style="display:none">
        <div class="file-list" id="fileList-expected"></div>
        <div class="file-reject" id="fileReject-expected"></div>
      </div>

      <div class="field">
        <label>실제 결과</label>
        <textarea name="actualResult"></textarea>
        <div class="dropzone dropzone-sm" id="dropzone-actual">스크린샷 첨부 (선택)</div>
        <input type="file" id="fileInput-actual" name="actualResultFiles" multiple accept="image/png,image/jpeg,image/gif,image/webp" style="display:none">
        <div class="file-list" id="fileList-actual"></div>
        <div class="file-reject" id="fileReject-actual"></div>
      </div>

      <div class="field">
        <label>개선 방향</label>
        <textarea name="suggestedFix"></textarea>
      </div>

      <div class="field">
        <label>첨부파일</label>
        <div class="dropzone" id="dropzone">
          이미지를 드래그하거나 클릭해서 선택하세요 (png/jpg/gif/webp)
        </div>
        <input type="file" id="fileInput" name="files" multiple accept="image/png,image/jpeg,image/gif,image/webp" style="display:none">
        <div class="file-list" id="fileList"></div>
        <div class="file-reject" id="fileReject"></div>
      </div>

      <div class="field">
        <label>처리 담당자</label>
        <select name="assigneeId">
          <option value="">미지정</option>
          <c:forEach var="u" items="${users}">
            <option value="${u.id}">${u.displayName}</option>
          </c:forEach>
        </select>
      </div>

      <div class="actions">
        <a class="btn" href="${ctx}/?projectId=${project.id}">취소</a>
        <button type="submit" class="btn btn-primary" id="submitButton">등록</button>
      </div>
        </form>
      </div>
    </section>
  </main>

  <script>
    var registerForm = document.getElementById('registerForm');
    var submitButton = document.getElementById('submitButton');
    var allowed = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
    var submitting = false;

    registerForm.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
        e.preventDefault();
      }
    });

    registerForm.addEventListener('submit', function (e) {
      if (submitting) {
        e.preventDefault();
        return;
      }
      submitting = true;
      submitButton.disabled = true;
      submitButton.textContent = '등록 중...';
    });

    /** 첨부 드롭존 하나(일반/기대결과/실제결과 각각)를 독립적으로 동작하게 만든다. */
    function setupDropzone(dropzoneId, fileInputId, fileListId, fileRejectId) {
      var dropzone = document.getElementById(dropzoneId);
      var fileInput = document.getElementById(fileInputId);
      var fileList = document.getElementById(fileListId);
      var fileReject = document.getElementById(fileRejectId);
      var selectedFiles = [];

      dropzone.addEventListener('click', function () { fileInput.click(); });
      dropzone.addEventListener('dragover', function (e) { e.preventDefault(); dropzone.classList.add('dragover'); });
      dropzone.addEventListener('dragleave', function () { dropzone.classList.remove('dragover'); });
      dropzone.addEventListener('drop', function (e) {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        addFiles(e.dataTransfer.files);
      });
      fileInput.addEventListener('change', function () {
        // fileInput.files는 일부 브라우저에서 fileInput 자신과 연결된 살아있는(live) 참조라서,
        // 미리 변수에 담아둬도 이후 fileInput.value를 지우면 같이 비어버린다.
        // File 객체 자체를 별도 배열로 복사해 완전히 분리한 뒤에 초기화해야 한다.
        var picked = Array.prototype.slice.call(fileInput.files);
        fileInput.value = '';
        addFiles(picked);
      });

      function addFiles(fileArrayLike) {
        var rejected = [];
        for (var i = 0; i < fileArrayLike.length; i++) {
          var f = fileArrayLike[i];
          if (allowed.indexOf(f.type) === -1) {
            rejected.push(f.name);
            continue;
          }
          selectedFiles.push(f);
        }
        fileReject.textContent = rejected.length ? ('허용되지 않는 형식이라 제외됨: ' + rejected.join(', ')) : '';
        renderFileList();
        syncInputFiles();
      }

      function renderFileList() {
        fileList.innerHTML = '';
        selectedFiles.forEach(function (f, idx) {
          var row = document.createElement('div');
          row.className = 'file-item';
          var kb = Math.round(f.size / 1024);
          row.innerHTML = '<span class="name"></span><span class="size mono">' + kb + ' KB</span>' +
            '<button type="button" class="remove" aria-label="제거">×</button>';
          row.querySelector('.name').textContent = f.name;
          row.querySelector('.remove').addEventListener('click', function () {
            selectedFiles.splice(idx, 1);
            renderFileList();
            syncInputFiles();
          });
          fileList.appendChild(row);
        });
      }

      function syncInputFiles() {
        var dt = new DataTransfer();
        selectedFiles.forEach(function (f) { dt.items.add(f); });
        fileInput.files = dt.files;
      }
    }

    setupDropzone('dropzone', 'fileInput', 'fileList', 'fileReject');
    setupDropzone('dropzone-expected', 'fileInput-expected', 'fileList-expected', 'fileReject-expected');
    setupDropzone('dropzone-actual', 'fileInput-actual', 'fileList-actual', 'fileReject-actual');
  </script>
</body>
</html>
