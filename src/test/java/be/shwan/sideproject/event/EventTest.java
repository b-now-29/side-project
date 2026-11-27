package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.*;

import be.shwan.sideproject.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {
  String testTitle = "테스트 쿠폰이벤트";
  int testTotalCount = 1000;

  @DisplayName("test1: 쿠폰 이벤트 생성")
  @Test
  void createEvent() {
    Event event =
        Event.create(
            testTitle, testTotalCount, LocalDateTime.now(), LocalDateTime.now().plusDays(1L));
    assertFalse(event.isEnd());
  }

  @DisplayName("test2: 쿠폰 이벤트 종료")
  @Test
  void endEvent() {
    Event event =
        Event.create(
            testTitle,
            testTotalCount,
            LocalDateTime.parse("2026-01-01T00:00:00"),
            LocalDateTime.parse("2026-01-31T00:00:00"));
    assertTrue(event.isEnd());
  }

  @DisplayName("test3: 쿠폰 이벤트 실패, 이벤트 시작시간보다 끝나는 시간이 빠름")
  @Test
  void createEventFail() {
    assertThrows(
        BusinessException.class,
        () ->
            Event.create(
                testTitle, testTotalCount, LocalDateTime.now(), LocalDateTime.now().minusDays(1)));
  }

  @DisplayName("test4: id가 null일때 동등성 비교")
  @Test
  void equalityTest() {
    Event event1 =
        Event.create(
            testTitle, testTotalCount, LocalDateTime.now(), LocalDateTime.now().plusDays(1L));
    Event event2 =
        Event.create(
            testTitle, testTotalCount, LocalDateTime.now(), LocalDateTime.now().plusDays(1L));
    assertNull(event1.getId());
    assertNull(event2.getId());
    assertNotEquals(event1, event2);
  }
}
