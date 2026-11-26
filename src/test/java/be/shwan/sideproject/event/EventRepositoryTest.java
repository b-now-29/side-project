package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Transactional
class EventRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:19");

  @Autowired EventRepository repository;

  @DisplayName("test1: 이벤트 등록")
  @Test
  void registerEvent() {
    Event event =
        Event.create("테스트 쿠폰이벤트", 1000, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    assertNotNull(event);
  }
}
