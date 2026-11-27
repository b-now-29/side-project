package be.shwan.sideproject.event;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping
  ResponseEntity<Void> createEvent(@Valid @RequestBody EventRequestDto eventRequestDto) {
    eventService.createEvent(eventRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
