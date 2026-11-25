# INTERVIEW

리뷰에서 나온 면접 질문과 정리된 답변. 답변은 개발자가 먼저 하고, 리뷰어가 채점한 뒤 정리된 답변을 덧붙인다.

## M1 — Environment

### 1차 리뷰 (2026-11-25)

**Q1. 이 프로젝트는 테스트 DB로 H2 대신 Testcontainers의 PostgreSQL을 쓴다. 왜 그런가? 대가는 무엇인가?**

- 개발자 답변: H2는 CI 환경에서 테스트할 수 없고 동시성 문제도 테스트하기 어렵다. Testcontainers의 PostgreSQL은 실제 운영과 똑같은 환경에서 DB 테스트를 할 수 있다.
- 채점: △ 부분 정답. "H2는 CI에서 못 돌린다"는 사실과 반대다. H2는 JVM 안에서 도는 in-memory DB라 CI에서 가장 돌리기 쉽다. Docker가 필요한 쪽은 오히려 Testcontainers다. "운영과 똑같은 환경"도 과장이다. 같은 것은 DB engine과 version이고, 서버 사양·설정·네트워크는 다르다. 대가에 대한 언급이 빠졌다.
- 정리된 답변: H2에 PostgreSQL compatibility mode가 있어도 SQL 문법만 흉내 낼 뿐이고, MVCC 구현, isolation level별 동작, row lock(`SELECT ... FOR UPDATE`) 처리 같은 locking 동작은 다르다. 이 프로젝트의 핵심은 동시 발급 시 over-issuance를 DB 수준에서 막는 것이다. 그래서 H2에서 통과한 테스트는 PostgreSQL에서의 동작을 보장하지 않는다. Testcontainers는 운영과 같은 engine과 version(이미지 tag를 고정할 때만)으로 테스트하게 해준다. 대가는 세 가지다. 로컬과 CI 모두 Docker가 필요하고, 컨테이너 기동 때문에 테스트가 느려지며, Docker가 꺼져 있으면 build가 깨진다(1차 리뷰에서 실제로 겪음). 이 대가는 context caching과 컨테이너 재사용으로 줄인다.

**Q2. `TestcontainersConfiguration`의 `@ServiceConnection`은 무엇을 대신해 주는가? 이게 없었다면 테스트가 컨테이너 DB에 붙게 하려고 무엇을 직접 써야 했나?**

- 개발자 답변: Docker PostgreSQL 이미지를 만들어서 연결한다. docker-compose.yaml 파일을 작성해야 한다.
- 채점: ✗ 오답. 이미지를 받아 컨테이너를 띄우는 것은 Testcontainers(`PostgreSQLContainer` bean)가 한다. `compose.yaml`은 테스트와 상관없고, `spring-boot-docker-compose`가 로컬 `bootRun` 때 쓰는 파일이다.
- 정리된 답변: Testcontainers는 컨테이너를 random host port로 띄운다. 그래서 JDBC URL은 실행할 때마다 바뀌고, `application.yaml`에 미리 적어둘 수 없다. `@ServiceConnection`은 떠 있는 컨테이너에서 접속 정보를 꺼내 `JdbcConnectionDetails` bean을 만들어 준다. Spring Boot의 DataSource auto-configuration은 `spring.datasource.*` property 대신 이 bean을 사용한다. 이게 없다면 `@DynamicPropertySource` static method에서 `registry.add("spring.datasource.url", postgres::getJdbcUrl)` 식으로 url, username, password를 직접 등록해야 했다. 컨테이너 lifecycle도 `@Testcontainers`와 `@Container`로 직접 관리해야 했다.

**Q3. 이전에는 War를 WebLogic에 배포했다. `./gradlew build`가 만드는 Spring Boot의 실행 가능한 Jar와 무엇이 다른가? 운영 관점에서 무엇이 바뀌는가?**

- 개발자 답변: War에는 웹서버가 없어서 따로 웹서버를 실행시켜야 한다. Jar는 Tomcat 웹서버를 내장하고 있다.
- 채점: △ 핵심은 맞다. 용어가 부정확하고, 운영 관점 답변이 빠졌다. Tomcat과 WebLogic은 "웹서버"가 아니다. Tomcat은 servlet container이고 WebLogic은 Java EE application server(WAS)다. "웹서버"는 보통 Apache httpd나 Nginx 같은 정적 리소스 서버와 reverse proxy를 말한다.
- 정리된 답변: War는 외부 WAS에 deploy되는 애플리케이션 묶음이다. 서버의 생명주기, 버전, DataSource(JNDI), 공유 라이브러리는 서버 쪽에서 관리한다. Spring Boot의 실행 가능한 Jar는 servlet container(webmvc starter의 기본값은 Tomcat)를 내장한다. 의존성까지 nested jar로 담겨 있어서 `java -jar` 하나로 기동된다. 운영 관점에서는 서버 버전과 설정(포트, thread pool, DataSource)이 `build.gradle`과 `application.yaml`로 들어와 애플리케이션이 직접 소유하고 버전 관리한다. 배포 단위는 "WAS에 올리는 War"에서 "프로세스 하나"가 된다. 그래서 Docker 이미지로 감싸 인스턴스 수로 scale-out하기 쉬워진다. 대신 WAS 관리 콘솔이 해주던 모니터링과 설정 관리는 Actuator나 외부 도구로 직접 챙겨야 한다.

**확인 질문 A. `TestcontainersConfiguration`에서 `@ServiceConnection`만 지우면 어떻게 되는가?**

- 개발자 답변: DataSource를 못 찾아서 Spring container가 뜨다가 에러가 난다.
- 채점: ○ 핵심은 맞다. 다만 무엇이 왜 실패하는지 더 정확히 말해야 한다.
- 정리된 답변: 컨테이너 bean은 그대로 있으므로 PostgreSQL 컨테이너는 정상적으로 뜬다. 하지만 `JdbcConnectionDetails`가 만들어지지 않아 Spring은 그 접속 정보를 모른다. `spring.datasource.url`도 없고 classpath에 embedded DB(H2 등)도 없으므로, DataSource auto-configuration이 "Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured" 류의 에러로 실패하고 ApplicationContext 생성이 중단된다. `compose.yaml`은 `developmentOnly`인 `spring-boot-docker-compose`용이라 테스트에서 대신 연결해 주지 않는다.

**확인 질문 B. `googleJavaFormat()`을 쓰면 `removeUnusedImports()`도 따로 필요한가?**

- 개발자 답변: `googleJavaFormat()`이 기본으로 지원한다.
- 채점: ○ 정답.
- 정리된 답변: google-java-format은 기본 동작으로 사용하지 않는 import를 제거하고 import 순서를 정렬한다. 그래서 `removeUnusedImports()`를 따로 둘 필요가 없다. 같은 일을 두 step이 하면 설정만 늘고 의도가 흐려진다.

### 2차 리뷰 (2026-11-25) — M1 통과

**Q4. CI workflow에서 `actions/checkout@v4`는 tag로, `gradle/actions/setup-gradle@af1da67...`은 commit SHA로 지정되어 있다. 차이는 무엇이고, 왜 SHA로 고정하는가?**

- 개발자 답변: 모르겠다. (조사 중)
- 조사 방향: tag는 누가, 언제, 어느 commit으로 옮길 수 있는가? SHA는 옮길 수 있는가? GitHub Docs의 "Security hardening for GitHub Actions" → "Using third-party actions" 섹션을 읽는다. 키워드는 supply chain.
- 정리된 답변: (조사 후 재답변 → 채점 후 작성)

**Q5. `./gradlew build`를 하면 포맷 검사가 왜 같이 실행되는가? 포맷이 틀린 코드가 PR로 올라오면 어디서, 어떻게 막히는가?**

- 개발자 답변: `./gradlew build`는 Gradle에 작성된 task를 전부 실행하기 때문에 CI 빌드 단계에서 실패로 뜬다. 현재 workflow는 main branch에 push할 때와 PR할 때 실행된다.
- 채점: △ 부분 정답. "task를 전부 실행한다"는 틀렸다. `spotlessApply`나 `bootRun`은 `build`에서 실행되지 않는다. Gradle은 요청한 task가 의존하는 task들의 그래프만 실행한다. workflow trigger 설명은 정확하다. 다만 CI가 red라고 해서 merge가 자동으로 막히지는 않는다.
- 정리된 답변: Spotless plugin은 `spotlessCheck` task를 등록하고, 이를 `check` task의 dependency로 연결한다. `build`는 `assemble`과 `check`에 의존하므로, `build` → `check` → `spotlessCheck` 순서로 task graph에 포함되어 실행된다. compile 단계에서 실행되는 것이 아니라 별도 task다. 포맷이 틀린 코드가 PR로 올라오면 `pull_request` trigger로 CI의 `./gradlew build`가 `spotlessCheck`에서 실패하고, PR에 red status check가 표시된다. 실제로 merge를 막으려면 branch protection(ruleset)에서 이 check를 required status check로 지정해야 한다. 그 전에 로컬에서 `./gradlew spotlessApply`나 build로 먼저 잡는 것이 피드백이 가장 빠르다. SI 시절 "있지만 강제되지 않던 style guide"와의 차이는 바로 이 강제 지점이 있느냐다.

**Q6. 첫 CI 실행(`b788d1b`)은 실패했지만 `build` job은 성공했다. 무엇이 실패했고 원인은 무엇이었나? 그 job을 지운 것은 맞는 판단이었나?**

- 개발자 답변: 모르겠다. (조사 중)
- 조사 방향: `gh run view 45576046495 --log-failed`로 `dependency-submission` job의 로그를 본다. 그 job은 무엇을 하려던 것인가? 동작하려면 repo 설정에서 무엇이 켜져 있어야 하는가? (`gradle/actions`의 `dependency-submission/README.md`) 같은 run에 찍힌 cache 서비스 장애 메시지가 원인이었는지, 설정 문제였는지 구분한다.
- 정리된 답변: (조사 후 재답변 → 채점 후 작성)
