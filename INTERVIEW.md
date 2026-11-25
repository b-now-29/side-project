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
