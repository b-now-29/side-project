package be.shwan.sideproject.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventServiceImp implements EventService {

  private final EventRepository eventRepository;

  public EventServiceImp(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Override
  @Transactional
  public Event createEvent(EventRequestDto requestDto) {
    return eventRepository.save(
        Event.create(
            requestDto.title(),
            requestDto.totalCount(),
            requestDto.eventStartDateTime(),
            requestDto.eventEndDateTime()));
  }
}
