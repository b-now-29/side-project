package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {
  @DisplayName("test1: 쿠폰 이벤트 생성")
  @Test
  void createEvent() {
    Event event = new Event();
    assertNotNull(event);
  }

  @DisplayName("test2: 쿠폰 이벤트 진행중")
  @Test
  void doingEvent() {
    Event event = new Event();
    Event createdEvent =
        event.create(LocalDateTime.of(2026, 11, 25, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59));
    assertTrue(!createdEvent.isEnd());
  }

  @DisplayName("test3: 쿠폰 이벤트 종료")
  @Test
  void endEvent() {
    Event event = new Event();
    Event createdEvent =
        event.create(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 6, 30, 23, 59));
    assertTrue(createdEvent.isEnd());
  }
}
