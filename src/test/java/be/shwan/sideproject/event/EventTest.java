package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {
  LocalDateTime testStartDateTime = LocalDateTime.of(2026, 11, 25, 0, 0);
  LocalDateTime testEndDateTime = LocalDateTime.of(2026, 12, 31, 23, 59);

  @DisplayName("test1: 쿠폰 이벤트 생성")
  @Test
  void createEvent() {
    Event event = Event.create("테스트 쿠폰이벤트", 1000, testStartDateTime, testEndDateTime);
    assertEquals(Event.create("테스트 쿠폰이벤트", 1000, testStartDateTime, testEndDateTime), event);
  }

  @DisplayName("test2: 쿠폰 이벤트 진행중")
  @Test
  void doingEvent() {
    Event event = Event.create(testStartDateTime, testEndDateTime);
    assertFalse(event.isEnd());
  }

  @DisplayName("test3: 쿠폰 이벤트 종료")
  @Test
  void endEvent() {
    Event event =
        Event.create(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 6, 30, 23, 59));
    assertTrue(event.isEnd());
  }
}
