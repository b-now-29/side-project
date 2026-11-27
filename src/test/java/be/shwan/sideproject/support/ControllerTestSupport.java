package be.shwan.sideproject.support;

import be.shwan.sideproject.event.EventController;
import be.shwan.sideproject.event.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EventController.class})
public abstract class ControllerTestSupport {
  @Autowired protected MockMvc mockMvc;
  @MockitoBean protected EventService eventService;
}
