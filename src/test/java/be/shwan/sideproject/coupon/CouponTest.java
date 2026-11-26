package be.shwan.sideproject.coupon;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponTest {
  @DisplayName("test1: 쿠폰 생성")
  @Test
  void createCoupon() {
    Coupon coupon = Coupon.create();
    assertNotNull(coupon);
  }
}
