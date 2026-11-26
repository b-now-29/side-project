package be.shwan.sideproject.coupon;

import static org.junit.jupiter.api.Assertions.*;

import be.shwan.sideproject.event.Event;
import be.shwan.sideproject.event.EventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Transactional
class CouponRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:19");

  @Autowired TestEntityManager em;
  @Autowired CouponRepository couponRepository;
  @Autowired EventRepository eventRepository;

  @DisplayName("test1: db저장 확인")
  @Test
  void createCoupon() {
    // given
    Event event =
        Event.create("test-event", 100, LocalDateTime.now(), LocalDateTime.now().plusDays(1L));

    // when
    Event savedEvent = eventRepository.save(event);
    em.flush();
    em.clear();
    Coupon coupon = Coupon.create(savedEvent, "test-1");
    Coupon savedCoupon = couponRepository.save(coupon);
    em.flush();
    em.clear();

    // then
    assertTrue(couponRepository.findByUserId("test-2").isEmpty());
    Coupon test1Coupon = couponRepository.findByUserId(("test-1")).getFirst();
    assertEquals("test-1", test1Coupon.getUserId());
    assertEquals(savedEvent.getId(), test1Coupon.getEvent().getId());
    assertEquals(savedEvent.getTitle(), test1Coupon.getEvent().getTitle());
  }
}
