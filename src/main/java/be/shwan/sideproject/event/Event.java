package be.shwan.sideproject.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Event {
  private static final Logger logger = LoggerFactory.getLogger(Event.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private LocalDateTime eventStartTime;
  @Column private LocalDateTime eventEndTime;
  @Column private LocalDateTime createAt;
  @Column private LocalDateTime updateAt;

  protected Event() {}

  public boolean end() {
    logger.debug("start: " + eventStartTime);
    logger.debug("end: " + eventEndTime);
    return eventStartTime.isAfter(eventEndTime);
  }

  public Event create(LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
    Event event = new Event();
    event.eventStartTime = eventStartTime;
    event.eventEndTime = eventEndTime;
    event.createAt = LocalDateTime.now();
    event.updateAt = LocalDateTime.now();
    return event;
  }

  public boolean isEnd() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(eventEndTime);
  }
}
