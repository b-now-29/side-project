package be.shwan.sideproject.support;

import be.shwan.sideproject.event.EventRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestSupport {
  @Mock protected EventRepository eventRepository;
}
