package be.shwan.sideproject.event;

import be.shwan.sideproject.exception.BusinessException;
import be.shwan.sideproject.exception.ErrorCode;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Event {
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
    if (eventStartDateTime.isAfter(eventEndDateTime))
      throw new BusinessException(ErrorCode.EARLY_END_DATE_TIME_THAN_START_DATE_TIME);
    Event event = new Event();
    event.title = title;
    event.totalCount = totalCount;
    event.eventStartDateTime = eventStartDateTime;
    event.eventEndDateTime = eventEndDateTime;
    event.createdAt = LocalDateTime.now();
    event.updatedAt = LocalDateTime.now();
    return event;
  }

  public boolean isEnd() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(eventEndDateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return id != null && Objects.equals(id, event.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public LocalDateTime getEventStartDateTime() {
    return eventStartDateTime;
  }

  public LocalDateTime getEventEndDateTime() {
    return eventEndDateTime;
  }
}
