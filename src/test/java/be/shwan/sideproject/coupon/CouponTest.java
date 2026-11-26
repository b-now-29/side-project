package be.shwan.sideproject.coupon;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import be.shwan.sideproject.event.Event;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponTest {
  @DisplayName("test1: 쿠폰 생성")
  @Test
  void createCoupon() {
    Coupon coupon =
        Coupon.create(
            Event.create("test-event", 100, LocalDateTime.now(), LocalDateTime.now().plusDays(1L)),
            "test-1");
    assertNotNull(coupon);
  }
}
