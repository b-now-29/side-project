package be.shwan.sideproject.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Event {
  private static final Logger logger = LoggerFactory.getLogger(Event.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_id", nullable = false)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int totalCount;

  @Column(nullable = false)
  private LocalDateTime eventStartDateTime;

  @Column(nullable = false)
  private LocalDateTime eventEndDateTime;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  protected Event() {}

  public static Event create(
      String title,
      int totalCount,
      LocalDateTime eventStartDateTime,
      LocalDateTime eventEndDateTime) {
    Event event = new Event();
    event.title = title;
    event.totalCount = totalCount;
    setupDefaultValue(event, eventStartDateTime, eventEndDateTime);
    return event;
  }

  public static Event create(LocalDateTime eventStartDateTime, LocalDateTime eventEndDateTime) {
    Event event = new Event();
    setupDefaultValue(event, eventStartDateTime, eventEndDateTime);
    return event;
  }

  private static void setupDefaultValue(
      Event event, LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
    event.eventStartDateTime = eventStartTime;
    event.eventEndDateTime = eventEndTime;
    event.createdAt = LocalDateTime.now();
    event.updatedAt = LocalDateTime.now();
  }

  public boolean isEnd() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(eventEndDateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return Objects.equals(id, event.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }
}
