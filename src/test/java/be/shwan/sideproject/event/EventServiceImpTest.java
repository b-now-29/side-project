package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import be.shwan.sideproject.support.ServiceTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("쿠폰 이벤트 생성")
class EventServiceImpTest extends ServiceTestSupport {

  EventService eventService;

  @BeforeEach
  void setup() {
    eventService = new EventServiceImp(eventRepository);
  }

  String testTitle = "test";
  int testTotalCount = 100;
  LocalDateTime testEventStartDateTime = LocalDateTime.now();
  LocalDateTime testEventEndDateTime = LocalDateTime.now().plusDays(1);

  @Test
  void test() {
    // given
    EventRequestDto requestDto =
        new EventRequestDto(
            testTitle, testTotalCount, testEventStartDateTime, testEventEndDateTime);

    eventService.createEvent(requestDto);
    ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    verify(eventRepository).save(captor.capture());
    Event value = captor.getValue();
    assertEquals(testTitle, value.getTitle());
    assertEquals(testTotalCount, value.getTotalCount());
    assertNotNull(value.getEventStartDateTime());
    assertNotNull(value.getEventEndDateTime());
  }
}
