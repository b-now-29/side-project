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

- 개발자 답변 (1차): 모르겠다. 이후 hint ladder 3단계까지 진행했다(tag 이동 시나리오와 annotated/lightweight tag 설명).
- 확인 질문: tag 이동 공격에 노출된 action은? 이 프로젝트에서는 어떻게 통일할 것인가?
- 개발자 답변 (2차): 노출된 것은 `checkout`과 `setup-java`. 모두 tag로 통일한다. 이유는 (1) 실력 향상을 위한 개인 프로젝트이고, (2) 버전 관리가 편하며, (3) Dependabot을 쓰지 않으므로 SHA로 두면 매번 사람이 확인해야 하기 때문이다.
- 채점: △ 1번은 정답이고, 2번의 결론도 받아들일 수 있다. 하지만 근거가 면접에서 통하지 않는다. "개인 프로젝트라서"는 "보안을 신경 쓰지 않았다"로 들린다. "Dependabot을 안 써서"는 순환 논리다. Dependabot은 설정 파일 하나면 되므로, 면접관은 곧바로 "그럼 왜 안 썼어요?"라고 묻는다. 좋은 근거는 위협 모델(피해 범위)과 action 제공자에 대한 신뢰 수준이다.
- 정리된 답변: `@v4` 같은 tag는 이름표(ref)라서 annotated든 lightweight든 관리자나 계정 탈취자가 `git tag -f`로 다른 commit에 옮길 수 있다. 그러면 내 `ci.yml`은 그대로인데 다른 코드가 내 CI에서 secrets와 `GITHUB_TOKEN` 권한으로 실행된다. 이것이 supply chain 공격이며, 2025년 3월 `tj-actions/changed-files` 사건이 실제 사례다. commit SHA는 내용으로 계산한 hash라서 다른 코드로 바꿔치기할 수 없다. 대신 업데이트를 자동으로 따라가지 못하므로 Dependabot(`github-actions` ecosystem)으로 갱신 PR을 받는 것이 보통이다. 이 프로젝트는 현재 CI에 secrets가 없고 `contents: read` 권한으로 build만 하므로, 탈취돼도 피해 범위가 작다. 그래서 GitHub 공식 actions(`actions/*`)는 편의를 위해 tag로 쓴다. 신뢰 경계 밖의 third-party action은 SHA로 고정하거나, 최소한 그 위험을 인지하고 선택한다. 배포(CD)가 추가되어 secrets가 생기는 시점에 전부 SHA와 Dependabot 조합으로 전환한다.

**Q5. `./gradlew build`를 하면 포맷 검사가 왜 같이 실행되는가? 포맷이 틀린 코드가 PR로 올라오면 어디서, 어떻게 막히는가?**

- 개발자 답변: `./gradlew build`는 Gradle에 작성된 task를 전부 실행하기 때문에 CI 빌드 단계에서 실패로 뜬다. 현재 workflow는 main branch에 push할 때와 PR할 때 실행된다.
- 채점: △ 부분 정답. "task를 전부 실행한다"는 틀렸다. `spotlessApply`나 `bootRun`은 `build`에서 실행되지 않는다. Gradle은 요청한 task가 의존하는 task들의 그래프만 실행한다. workflow trigger 설명은 정확하다. 다만 CI가 red라고 해서 merge가 자동으로 막히지는 않는다.
- 정리된 답변: Spotless plugin은 `spotlessCheck` task를 등록하고, 이를 `check` task의 dependency로 연결한다. `build`는 `assemble`과 `check`에 의존하므로, `build` → `check` → `spotlessCheck` 순서로 task graph에 포함되어 실행된다. compile 단계에서 실행되는 것이 아니라 별도 task다. 포맷이 틀린 코드가 PR로 올라오면 `pull_request` trigger로 CI의 `./gradlew build`가 `spotlessCheck`에서 실패하고, PR에 red status check가 표시된다. 실제로 merge를 막으려면 branch protection(ruleset)에서 이 check를 required status check로 지정해야 한다. 그 전에 로컬에서 `./gradlew spotlessApply`나 build로 먼저 잡는 것이 피드백이 가장 빠르다. SI 시절 "있지만 강제되지 않던 style guide"와의 차이는 바로 이 강제 지점이 있느냐다.

**Q6. 첫 CI 실행(`b788d1b`)은 실패했지만 `build` job은 성공했다. 무엇이 실패했고 원인은 무엇이었나? 그 job을 지운 것은 맞는 판단이었나?**

- 개발자 답변: 모르겠다. (조사 중)
- 조사 방향: `gh run view 45576046495 --log-failed`로 `dependency-submission` job의 로그를 본다. 그 job은 무엇을 하려던 것인가? 동작하려면 repo 설정에서 무엇이 켜져 있어야 하는가? (`gradle/actions`의 `dependency-submission/README.md`) 같은 run에 찍힌 cache 서비스 장애 메시지가 원인이었는지, 설정 문제였는지 구분한다.
- 정리된 답변: (조사 후 재답변 → 채점 후 작성)

## M2 — Basic API

### 작업 중 질문 (2026-11-25)

**Q7. `.githooks/pre-commit`을 `core.hooksPath`로 연결했다. 다른 사람이 이 repo를 clone하면 hook이 동작하는가? CI의 `spotlessCheck`와 이 hook은 각각 어떤 역할을 맡는가?**

- 개발자 답변: (1) 동작하지 않는다. clone한 사람도 `git config core.hooksPath .githooks`를 한 번 실행해야 한다. (2) hook은 commit 전에 포맷이 흐트러진 코드를 놓쳤을 때, CI build 단계까지 가서 알아차리기 전에 미리 알아차리고 수정하게 해준다.
- 채점: (1) ○ 정답. 다만 "왜 그렇게 설계됐는가"까지 말하면 더 좋다. (2) △ hook의 역할은 맞다. 하지만 CI의 역할과 둘의 관계가 빠졌다. 핵심은 "hook은 우회할 수 있다"는 점이다.
- 정리된 답변: `core.hooksPath`는 `.git/config`에 저장되고, `.git/` 디렉터리는 버전 관리 대상이 아니라서 clone할 때 따라가지 않는다. 이는 보안 때문에 의도된 설계다. clone만으로 hook이 설치된다면 남의 repo를 받는 순간 임의의 코드가 내 PC에서 실행될 수 있다. 그래서 README에 설정 방법을 적거나, Gradle task로 설정을 자동화하는 식으로 보완한다. 역할은 이렇게 나뉜다. hook은 **빠른 피드백**을 주는 개인용 편의 장치다. 설치하지 않을 수도 있고 `git commit --no-verify`로 건너뛸 수도 있으므로 강제력이 없다. CI의 `spotlessCheck`는 **최종 관문(gate)**이다. branch protection의 required status check와 함께 쓰면 누구도 우회할 수 없다. 따라서 hook은 CI를 대체하지 않는다. "CI에서 떨어질 일을 로컬에서 미리 잡아 피드백 시간을 줄이는" 보조 수단이다. 정리하면 "강제는 CI에서, 편의는 hook에서"다.

### 작업 중 질문 (2026-11-26) — gitleaks 도입

**Q8. gitleaks를 pre-commit hook에서 돌리는데 CI에서 한 번 더 스캔하는 이유는? hook은 `--staged`로, CI는 `fetch-depth: 0`으로 스캔한다. 범위는 어떻게 다르고 왜 그렇게 나누는가?**

- 개발자 답변: (1) hook은 `--no-verify` 옵션으로 회피할 수 있고, CI 확인은 우회가 불가능하다. (2) pre-commit은 현재 staging된 파일만, `fetch-depth: 0`은 commit 기록 전체를 본다. 이유는 모르겠다.
- 채점: (1) △ 방향은 맞다. 하지만 표현이 부정확하고 secret만의 핵심이 빠졌다. `--no-verify`가 건너뛰는 것은 commit message가 아니라 `pre-commit`·`commit-msg` hook이다. clone하면 hook이 설치되지 않는다는 점(Q7)도 빠졌다. "CI는 우회 불가능"은 required status check로 지정했을 때만 맞다. 무엇보다 secret은 CI가 잡는 시점에 이미 remote(public repo)에 올라가 있다. (2) △ 범위 설명은 맞다. 이유는 답하지 못했다.
- 정리된 답변: hook은 설치하지 않을 수도 있고(`core.hooksPath`는 clone해도 따라오지 않는다) `git commit --no-verify`로 건너뛸 수도 있다. 그래서 CI가 최종 관문을 맡는다. 다만 secret은 포맷과 성격이 다르다. 포맷 오류는 CI에서 막으면 merge 전이라 피해가 없다. 하지만 secret은 push하는 순간 public repo에 노출된다. 이미 bot이 수집했을 수 있으므로 CI가 red여도 피해는 이미 생긴 뒤다. 그래서 secret에서는 CI가 "막는 장치"가 아니라 "탐지해서 key 폐기(rotate)를 시작하게 하는 장치"다. 유출 자체를 막는 것은 push 전에 동작하는 hook뿐이다. 그래서 포맷보다 secret에서 hook의 가치가 훨씬 크다.
  범위가 다른 이유는 이렇다. hook은 commit마다 실행되므로 빨라야 한다. 새로 들어가는 내용은 staged diff뿐이고, 이전 commit들은 각자 commit될 때 이미 검사를 거쳤다. staged만 보는 이유는 그것이 정확히 이번 commit에 들어갈 내용이기 때문이다(unstaged 변경은 commit되지 않는다). CI는 hook을 거치지 않은 commit이 섞여 들어올 수 있다고 가정해야 한다. 그리고 commit 1에서 secret을 추가하고 commit 2에서 지우면, 최종 파일 상태는 깨끗해도 history에는 남아서 `git log -p`로 누구나 볼 수 있다. 그래서 파일 상태가 아니라 push·PR에 포함된 commit 하나하나를 스캔해야 한다. `actions/checkout`은 기본값(`fetch-depth: 1`)으로 최신 commit 하나만 받으므로, `fetch-depth: 0`으로 history를 받아야 그 commit 범위를 스캔할 수 있다. 같은 이유로 history에 들어간 secret은 파일을 지워도 사라지지 않는다. 그래서 push 전이면 commit을 고치고, push 후면 key를 폐기한다.

### 작업 중 질문 (2026-11-26) — table 설계

**Q9. Java field는 camelCase, DB column은 snake_case다. Spring Boot에서 둘은 어떻게 연결되는가? `@Column(nullable = false)`는 무엇을 보장하는가?**

- 개발자 답변: (1) Java는 camelCase, DB는 snake_case가 관례이고 Spring Boot도 이 관례를 따라 snake_case column을 만든다. (2) null이면 안 되는 field에 `@Column(nullable = false)` 같은 옵션을 붙인다.
- 채점: (1) ○ 정답. 다만 그 변환을 누가 하는지(구체적인 class 이름)까지 말하면 더 좋다. (2) △ 어디에 표시하는지는 맞다. 하지만 이 옵션이 언제, 무엇에 영향을 주는지가 빠졌다.
- 정리된 답변: Spring Boot는 Hibernate의 physical naming strategy를 기본값 `CamelCaseToUnderscoresNamingStrategy`로 설정한다. 그래서 `createdAt` field는 `created_at` column에 매핑된다. `@Column(nullable = false)`는 Hibernate가 **schema를 생성할 때**(`ddl-auto`) column에 `NOT NULL`을 붙이라는 metadata다. schema를 Flyway 같은 migration으로 관리하면 DDL에 영향을 주지 않는다. 그 경우 실제로 null을 막는 것은 DB의 `NOT NULL` 제약이다. 입력값을 application 단에서 미리 검증하려면 Bean Validation(`@NotNull`)을 따로 쓴다. `@NotNull`이 언제 검사되는지는 **어디에 붙였는지**에 따라 다르다. request DTO에 붙이고 controller parameter에 `@Valid`를 달면 request binding 시점에 검사되고, 실패하면 400 응답이 나간다. entity에 붙이면 Hibernate의 Bean Validation 연동이 persist/update 직전(flush 전)에 검사하고, 실패하면 `ConstraintViolationException`이 발생한다. 정리하면 "최종 보장은 DB 제약, 빠른 피드백은 application 검증"이다.
- 추가 대화(2026-11-26): 개발자는 "`nullable = false`는 insert/update할 때, `@NotNull`은 HTTP 요청 받을 때"라고 답했다. 둘 다 반쯤 맞다. `nullable`은 DDL 생성 시점에 쓰이는 metadata이고, insert/update를 실제로 거부하는 것은 그 결과로 만들어진 DB 제약이다. `@NotNull`의 검사 시점은 붙인 위치에 따라 다르다(위 설명 참고).

**Q10. `@ManyToOne`의 기본 fetch 전략은? "내 쿠폰 목록"이 쿠폰 10장을 조회하면 SQL은 몇 번 실행되는가?**

- 개발자 답변: `EAGER`, 11번.
- 채점: (1) ○ 정답. (2) △ 최악의 경우에만 맞다. 횟수를 결정하는 조건이 빠졌다.
- 정리된 답변: JPA 표준에서 `@ManyToOne`·`@OneToOne`의 기본값은 `EAGER`이고, `@OneToMany`·`@ManyToMany`는 `LAZY`다. Spring Data의 `findByUserId` 같은 JPQL query는 fetch 전략을 무시하고 쿠폰만 먼저 조회한다(1번). 그다음 EAGER를 채우려고 연관된 Event를 따로 조회한다. 이때 같은 Event는 영속성 컨텍스트(1차 cache)에 이미 있으므로 다시 조회하지 않는다. 그래서 추가 SQL 수는 쿠폰 수가 아니라 **서로 다른 event 수**다. 10장이 모두 다른 이벤트면 1 + 10 = 11번, 모두 같은 이벤트면 1 + 1 = 2번이다. 이것이 N+1 문제다. 그래서 연관관계는 기본적으로 `LAZY`로 두고, Event가 필요한 조회에서만 fetch join이나 `@EntityGraph`로 한 번에 가져온다. `em.find()`처럼 id로 단건 조회할 때는 Hibernate가 EAGER 연관을 join으로 함께 가져오므로 이 문제가 드러나지 않는다.

### 작업 중 질문 (2026-11-26) — entity 연관관계

**Q11. `Coupon.event`(`@ManyToOne`)에 `cascade = CascadeType.PERSIST`를 걸면 어떻게 되는가? cascade는 언제 쓰는가?**

- 개발자 답변: (1) Coupon에서 Event 정보에 접근할 때 사용자 id로 조건을 걸지 않으면 100건이 한 번에 넘어온다. (2) Coupon entity가 Event entity와 생명주기를 같이 가질 때 쓴다.
- 채점: (1) × cascade는 조회와 관계가 없다. persist·remove 같은 **상태 변경을 연관 entity로 전파**하는 설정이다. (2) △ "생명주기"라는 핵심어는 맞다. 하지만 전파 방향과 소유 관계가 빠졌다.
- 정리된 답변: cascade는 부모에 한 작업(persist, remove 등)을 연관된 자식에게도 똑같이 적용한다. `Coupon → Event` 방향으로 PERSIST를 걸면 쿠폰을 저장할 때 Event도 persist하려고 한다. Event가 transient이면 발급 요청마다 새 Event row가 insert되어 이벤트가 중복으로 만들어진다. Event가 detached이면(예: `em.clear()` 이후의 Event) `PersistentObjectException: detached entity passed to persist`가 발생한다. 근본적인 문제는 방향이다. Event는 관리자가 먼저 만들고 쿠폰보다 오래 사는 독립된 entity다. 쿠폰 하나가 Event의 생명주기를 결정할 수 없다. cascade는 자식의 생명주기가 **단일 부모에게 완전히 종속될 때**, 부모 → 자식 방향으로 건다(예: `Order` → `OrderItem`, 부모 저장·삭제 시 함께 저장·삭제). 여러 entity가 공유하는 대상이나 독립적으로 관리되는 대상에는 걸지 않는다. `REMOVE`는 특히 위험하다. 쿠폰 하나를 지울 때 Event까지 지워질 수 있다.

**Q12. 저장한 Coupon과 DB에서 다시 조회한 Coupon을 `equals`(모든 field 비교)로 비교했더니 실패했다. 왜인가? entity의 `equals`는 어떻게 설계해야 하는가?**

- 개발자 답변: (1) LAZY로 설정했고 아직 참조하지 않아서 `event`가 Hibernate proxy(껍데기)로 되어 있다. 그래서 `getClass()`가 다르다. (2) `createdAt`이 미세하게 다르다. (3) 테스트하는 방법이 잘못된 것인가?
- 채점: (1) ○ 디버거로 직접 확인한 점이 좋다. 다만 proxy는 초기화된 뒤에도 class가 바뀌지 않는다는 점까지 알아야 한다. (2) △ 현상은 맞다. 원인(정밀도 차이)을 말하지 못했다. (3) ○ 맞다. 테스트를 통과시키려고 production의 `equals`를 바꾼 것이 잘못된 방향이었다.
- 정리된 답변: LAZY 연관은 Hibernate가 만든 `Event`의 **subclass proxy**로 채워진다. 그래서 `getClass()`로 비교하면 실제 데이터와 상관없이 항상 다르게 나온다. proxy는 초기화된 뒤에도 class가 바뀌지 않는다. 또 proxy 객체에 대해 다른 객체의 field에 직접 접근하면(`event.title`) proxy 자신의 빈 field를 읽는다. 초기화를 일으키려면 getter를 거쳐야 한다. `createdAt`이 달랐던 이유는 정밀도 차이다. Java의 `LocalDateTime.now()`는 OS에 따라 nanosecond 단위까지 값을 가질 수 있다. 반면 PostgreSQL의 `timestamp`는 microsecond(소수점 6자리)까지만 저장하므로, 왕복하는 사이 값이 반올림된다. 그래서 entity의 `equals`는 보통 **식별자(id) 기반**으로 두고, proxy를 고려해 `instanceof`와 getter로 비교한다. 테스트에서는 `equals`에 기대지 않고 검증하려는 값(`userId`, `event.getId()`)을 직접 꺼내서 비교한다. proxy의 `getId()`는 초기화 없이 id를 돌려주므로 추가 SQL이 실행되지 않는다. 시각 값은 테스트에서 고정값을 쓰거나 `truncatedTo(ChronoUnit.MICROS)`로 맞춘다.
- 추가 대화(2026-11-26): proxy의 `getId()`를 호출했을 때는 `Event.getId()` 본문의 `println`이 실행되지 않았다. 반면 `getTitle()`을 호출했을 때는 `select`가 실행된 뒤 본문의 `println`이 실행됐다. proxy는 모든 메서드를 override하고, 각 호출을 interceptor(`LazyInitializer`)에 넘긴다. interceptor는 식별자 getter이면 자신이 보관한 id를 바로 반환한다. 이때 진짜 객체는 아직 존재하지 않는다. 그 밖의 메서드이면 진짜 객체가 초기화되지 않은 경우 `select`로 초기화한 다음, 진짜 객체의 메서드를 호출한다. proxy는 "마지막 호출"을 기다리는 것이 아니다. **id가 아닌 데이터가 처음 필요한 순간** 초기화한다. 영속성 컨텍스트가 닫힌 뒤(transaction 밖)에 이 첫 접근이 일어나면 `LazyInitializationException`이 발생한다.

**Q13. Service의 transaction이 끝난 뒤 Controller에서 LAZY 연관(`coupon.getEvent().getTitle()`)에 접근하면 어떻게 되는가? Spring Boot는 왜 `spring.jpa.open-in-view` 경고를 띄우는가?**

- 개발자 답변: (1) transaction이 끝났으니 호출할 수 없다는 에러가 난다. (2) 기본값은 `true`이고, 렌더링된 view가 닫힐 때까지 session이 열려 있어서 title 조회 query가 실행된다. (3) 좋은 것이 아니다. 실제로 쓰지 않는 요청이 DB connection pool의 connection을 점유한다. 그러면 다른 요청들이 매번 DB 연결·해제를 해야 해서 응답 속도가 느려진다.
- 채점: (1) ○ OSIV가 꺼져 있을 때의 동작이다. 이름은 `LazyInitializationException`(`could not initialize proxy - no Session`)이다. (2) ○ 정답. REST API라면 "view 렌더링"보다 "요청 처리가 끝나 응답이 쓰일 때까지"가 더 정확하다. (3) △ connection을 점유한다는 문제 인식은 맞다. 그 결과는 틀렸다. pool은 connection을 재사용하므로 매번 연결·해제하지 않는다. pool이 바닥나면 다른 요청은 connection을 **기다린다**.
- 정리된 답변: OSIV(Open Session In View)는 기본값이 `true`다. 이 상태에서는 영속성 컨텍스트가 요청 시작부터 응답이 끝날 때까지 열려 있다. 그래서 transaction 밖인 Controller에서도 LAZY 로딩이 동작한다(이때 query는 transaction 없이 auto-commit으로 실행된다). 대가는 **DB connection 점유 시간**이다. 한 번 DB에 접근하면 그 요청이 끝날 때까지 connection을 붙잡는다. 외부 API 호출이나 JSON 직렬화처럼 DB와 무관한 시간까지 포함된다. connection pool(HikariCP 기본 10개)은 connection을 재사용하므로 매번 새로 연결하지는 않는다. 하지만 점유 시간이 길어지면 pool이 바닥나고, 이후 요청은 `connectionTimeout`(기본 30초)까지 대기하다가 실패한다. 동시 요청이 몰리는 선착순 쿠폰 시스템에서 특히 치명적이다. 그래서 OSIV를 끄고, 응답에 필요한 데이터를 Service 안(transaction 안)에서 fetch join이나 DTO 변환으로 모두 준비한다. 이 방식은 연관 데이터를 언제 조회하는지가 Service 코드에 명시적으로 드러난다는 장점도 있다. Spring Boot가 경고를 띄우는 이유는, 편리한 기본값이 이런 숨은 비용을 만든다는 것을 개발자가 알고 선택하게 하려는 것이다.
