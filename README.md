# QA로그

팀원이 오류를 등록 → 담당자가 수정 → 종료 → 필요 시 재오픈하는 전 과정을 사내망에서 공동 관리하는 웹앱.
NCI IPP 자율학습 프로젝트 (실무 프로젝트 아님).

## 스택

- Java 8 · 전자정부 표준프레임워크(eGovFrame) 실행환경 **3.8.0** · Spring 4.3.16 · MyBatis 3.4.6
- Maven (war 패키징) · JSP/JSTL · **PostgreSQL(Supabase)** — 2026-09-11 선임 지시로 MySQL/MariaDB에서 전환
- 회사 실제 프로젝트(RIMS_APPLY/RIMS_BMS)와 같은 eGovFrame Maven 저장소 사용. 다만 그 프로젝트들은
  최신 버전(4.2.0, `org.egovframe.rte` groupId)이고 이 프로젝트는 3.8.0(`egovframework.rte` groupId,
  구 명명 규칙)을 쓰므로 좌표가 다르다.

## 처음 실행하기

1. 팀 Supabase 프로젝트(Organization에 초대받은 계정으로 로그인)에서 SQL Editor를 열고
   [`ddl/schema.sql`](ddl/schema.sql)을 실행한다.
2. `src/main/resources/db.properties.example`을 같은 폴더에 `db.properties`로 복사한다. Supabase
   프로젝트의 Settings → Database → Connection string(JDBC/URI)에서 host를 그대로 복사하고,
   비밀번호는 팀에서 공유받은 값을 채운다. (`db.properties`는 `.gitignore` 대상이라 git에는 올라가지
   않는다 — 다 같은 DB에 붙는 것이므로 로컬 사본이 아니라 실제 공유 데이터를 보게 된다.)
   같은 파일에 `attachment.storage.path`도 **절대경로**로 반드시 채운다(예:
   `C:/Users/본인계정/qalog-uploads`) — 상대경로를 쓰면 `mvn tomcat7:run`을 실행하는 폴더가
   바뀔 때마다 첨부파일 저장 위치가 달라져 예전 첨부를 못 찾는 문제가 생긴다. (참고: 경로에 한글이
   섞여도 되도록 `root-context.xml`에서 설정 파일 인코딩을 UTF-8로 명시해뒀다 - 처음엔 이게 없어서
   한글 경로가 깨진 이름의 폴더로 만들어지는 문제가 있었음, 2026-09-14 수정.)
3. 별도 Tomcat 설치 없이 바로 띄우려면:
   ```
   mvn tomcat7:run
   ```
   `http://localhost:8080/` 접속 시 배선 확인용 임시 화면(등록된 프로젝트 목록)이 보이면 정상.
   실제 오류 상세 화면은 `http://localhost:8080/issues/{id}` (DB에 프로젝트·사용자·오류 데이터가
   최소 1건씩 있어야 함 — 아직 등록 화면이 없어 SQL Editor로 직접 넣어야 한다).
4. 사내 서버 등 외부 Tomcat에 배포하려면 `mvn package`로 만든 `target/qalog.war`를 그대로 올리면 된다.

## 패키지 구조

도메인 단위로 `controller / service / service.impl / mapper / vo`를 분리한다(회사 컨벤션과 동일).
Mapper.xml도 도메인별로 분리해 `src/main/resources/egovframework/sqlmap/mappers/`에 둔다
(`projects.xml`, `users.xml`, `issues.xml`).

```
src/main/java/egovframework/
  common/    도메인 공통 (SessionKeys — 로그인 세션 키 이름, 동기 로그인 구현과 맞춰야 함)
  project/   완성 예시 — Controller→Service→Mapper→DB 배선 확인용 (목록 조회/생성만)
  user/      VO + 로그인 구현 전까지 최소 조회만(selectDisplayName, selectAllForOptions).
             가입/로그인 자체는 아직 없음 (동기 담당)
  issue/     오류 상세/등록/이력/댓글/첨부 전체 구현 완료 — controller(JSON `/api/issues/*` +
             화면용 `/issues/{id}` IssueViewController) / dto / exception / gubun(상태값 enum) /
             mapper / service / vo
```

JSP 화면은 `src/main/webapp/WEB-INF/views/issue/detail.jsp`(오류 상세) 하나만 있다. 오류 등록 폼
JSP, 메인 화면(좌측 프로젝트 목록, 동기 담당)은 아직 없다.

## 주의

- `/api/*`는 JSON API, `/issues/{id}` 같은 접두사 없는 경로는 JSP를 반환하는 화면용 경로다
  (`docs/오류상세_기능명세서.md` 참고).
- 로그인이 없어 `SessionKeys.LOGIN_USER_ID`가 항상 비어있다 — 담당자 변경/댓글 작성 등에서
  "누가 했는지"가 기록되지 않는다. 로그인 구현 시 이 키로 세션에 사용자 id를 넣어줘야 한다.
- DB 스키마는 [`ddl/schema.sql`](ddl/schema.sql)이 기준이다. 필드를 바꾸면 이 파일과
  `writing-block.md`, 동기가 가진 ERD를 함께 갱신할 것.
- 실제 DB(Supabase) 기동 테스트는 아직 못 해봤다 — `mvn package` 빌드 검증까지만 완료.
