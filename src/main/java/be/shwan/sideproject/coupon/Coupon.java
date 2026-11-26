package be.shwan.sideproject.coupon;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Coupon {
  private static final Logger logger = LoggerFactory.getLogger(Coupon.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private LocalDateTime createAt;

  @Column private LocalDateTime updateAt;

  protected Coupon() {}

  public static Coupon create() {
    return new Coupon();
  }
}
