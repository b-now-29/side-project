package be.shwan.sideproject.coupon;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
  List<Coupon> findByUserId(String userId);
}
