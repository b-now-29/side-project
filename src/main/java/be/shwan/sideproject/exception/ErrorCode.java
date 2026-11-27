package be.shwan.sideproject.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INVALID_INPUT_DATE_TIME(HttpStatus.BAD_REQUEST, "날짜 입력이 잘못됐습니다."),
  EARLY_END_DATE_TIME_THAN_START_DATE_TIME(HttpStatus.BAD_REQUEST, "이벤트 종료 시간이 이벤트 시작 시간 보다 빠릅니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
