# QA로그

팀원이 오류를 등록 → 담당자가 수정 → 종료 → 필요 시 재오픈하는 전 과정을 사내망에서 공동 관리하는 웹앱.
NCI IPP 자율학습 프로젝트 (실무 프로젝트 아님).

## 스택

- Java 8 · 전자정부 표준프레임워크(eGovFrame) 실행환경 **3.8.0** · Spring 4.3.16 · MyBatis 3.4.6
- Maven (war 패키징) · JSP/JSTL · MySQL/MariaDB
- 회사 실제 프로젝트(RIMS_APPLY/RIMS_BMS)와 같은 eGovFrame Maven 저장소 사용. 다만 그 프로젝트들은
  최신 버전(4.2.0, `org.egovframe.rte` groupId)이고 이 프로젝트는 3.8.0(`egovframework.rte` groupId,
  구 명명 규칙)을 쓰므로 좌표가 다르다.

## 처음 실행하기

1. MySQL/MariaDB에 `qalog` 데이터베이스를 만들고 [`ddl/schema.sql`](ddl/schema.sql)을 실행한다.
2. `src/main/resources/db.properties.example`을 같은 폴더에 `db.properties`로 복사한 뒤 실제 접속정보를 채운다.
   (`db.properties`는 `.gitignore` 대상이라 git에는 올라가지 않는다.)
3. 별도 Tomcat 설치 없이 바로 띄우려면:
   ```
   mvn tomcat7:run
   ```
   `http://localhost:8080/` 접속 시 배선 확인용 임시 화면(등록된 프로젝트 목록)이 보이면 정상.
4. 사내 서버 등 외부 Tomcat에 배포하려면 `mvn package`로 만든 `target/qalog.war`를 그대로 올리면 된다.

## 패키지 구조

도메인 단위로 `controller / service / service.impl / mapper / vo`를 분리한다(회사 컨벤션과 동일).
Mapper.xml도 도메인별로 분리해 `src/main/resources/egovframework/sqlmap/mappers/`에 둔다
(`projects.xml`, `users.xml`, `issues.xml`).

```
src/main/java/egovframework/
  project/   완성 예시 — Controller→Service→Mapper→DB 배선 확인용 (목록 조회/생성만)
  user/      VO + 빈 Mapper 인터페이스만. 가입/로그인 구현 시 채울 것 (동기 담당)
  issue/     VO(전체 필드 포함) + 빈 Mapper 인터페이스만. 오류 상세/등록 구현 시 채울 것
```

`issue_attachments` / `issue_comments` / `issue_histories`는 아직 Java 클래스가 없다 — DDL에는
이미 반영돼 있으니(`ddl/schema.sql`), 첨부·댓글·이력 기능을 실제로 만들 때 VO/Mapper를 추가하면 된다.

## 주의

- `/`와 `/api/projects`는 배선이 도는지 확인하기 위한 임시 화면/API다. 실제 메인 화면(좌측 프로젝트
  목록 UI)은 별도로 설계해서 `ProjectController`/`health.jsp`를 대체해야 한다.
- DB 스키마는 [`ddl/schema.sql`](ddl/schema.sql)이 기준이다. 필드를 바꾸면 이 파일과
  `writing-block.md`를 함께 갱신할 것.
