package be.shwan.sideproject.exception;

import java.io.Serial;

public class BusinessException extends RuntimeException {

  @Serial private static final long serialVersionUID = -3017015879638795637L;
  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
