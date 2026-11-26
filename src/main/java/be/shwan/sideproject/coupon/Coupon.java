package be.shwan.sideproject.coupon;

import be.shwan.sideproject.event.Event;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Coupon {
  private static final Logger logger = LoggerFactory.getLogger(Coupon.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "coupon_id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  protected Coupon() {}

  public static Coupon create(Event event, String userId) {
    Coupon coupon = new Coupon();
    coupon.event = event;
    coupon.userId = userId;
    coupon.createdAt = LocalDateTime.now();
    coupon.updatedAt = LocalDateTime.now();
    return coupon;
  }

  public Long getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Coupon coupon = (Coupon) o;
    return Objects.equals(id, coupon.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public String getUserId() {
    return userId;
  }

  public Event getEvent() {
    return event;
  }
}
