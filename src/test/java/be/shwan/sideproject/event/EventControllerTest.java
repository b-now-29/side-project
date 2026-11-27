package be.shwan.sideproject.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import be.shwan.sideproject.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class EventControllerTest extends ControllerTestSupport {

  @DisplayName("test1: 쿠폰이벤트 생성")
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
                        "totalCount": 0,
                        "eventStartDateTime": "2026-11-01T00:00:00",
                        "eventEndDateTime" : "2026-11-30T23:59:00"
                    }
                    """))
        .andExpect(status().is4xxClientError());
  }
}
