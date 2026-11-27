package be.shwan.sideproject.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import be.shwan.sideproject.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class EventIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvc mockMvc;

  @Autowired private EventRepository eventRepository;

  @DisplayName("test1: 유효한 요청이면 쿠폰 이벤트를 생성한다")
  @Test
  void createEventWithValidRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "test",
                        "totalCount": 100,
                        "eventStartDateTime": "2026-11-01T00:00:00",
                        "eventEndDateTime" : "2026-11-30T23:59:00"
                    }
                    """))
        .andExpect(status().isCreated());

    assertEquals(1, eventRepository.findAll().size());
    Event event = eventRepository.findAll().getFirst();
    assertEquals("test", event.getTitle());
    assertEquals(100, event.getTotalCount());
    assertEquals(LocalDateTime.parse("2026-11-01T00:00:00"), event.getEventStartDateTime());
    assertEquals(LocalDateTime.parse("2026-11-30T23:59:00"), event.getEventEndDateTime());
  }

  @DisplayName("test2: 잘못된 쿠폰 이벤트 생성 요청")
  @Test
  void createEventWithWrongRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "test",
                        "totalCount": 100,
                        "eventStartDateTime": "2026-11-01T00:00:00",
                        "eventEndDateTime" : "2026-10-30T23:59:00"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("EARLY_END_DATE_TIME_THAN_START_DATE_TIME"))
        .andExpect(jsonPath("$.code").value("EARLY_END_DATE_TIME_THAN_START_DATE_TIME"))
        .andExpect(jsonPath("$.detail").value("이벤트 종료 시간이 이벤트 시작 시간 보다 빠릅니다."));
  }
}
