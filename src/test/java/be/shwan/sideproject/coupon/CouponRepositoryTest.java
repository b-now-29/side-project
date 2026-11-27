package be.shwan.sideproject.coupon;

import static org.junit.jupiter.api.Assertions.*;

import be.shwan.sideproject.event.Event;
import be.shwan.sideproject.event.EventRepository;
import be.shwan.sideproject.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CouponRepositoryTest extends RepositoryTestSupport {

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
