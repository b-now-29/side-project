package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import be.shwan.sideproject.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventRepositoryTest extends RepositoryTestSupport {

  @Autowired EventRepository eventRepository;

  @DisplayName("test1: 이벤트 등록")
  @Test
  void registerEvent() {
    // given

    // when

    // then
    Event event =
        Event.create("테스트 쿠폰이벤트", 1000, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    Event save = eventRepository.save(event);
    assertNotNull(save.getId());
  }
}
