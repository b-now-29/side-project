package be.shwan.sideproject.exception;

import java.util.HashMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // 도메인/비즈니스 예외
  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusiness(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(errorCode.getStatus(), errorCode.getMessage());
    problemDetail.setTitle(errorCode.name());
    problemDetail.setProperty("code", errorCode.name());
    return problemDetail;
  }

  // @Valid 실패

  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "입력값이 올바르지 않습니다.");
    HashMap<String, String> errorMap = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(e -> errorMap.put(e.getField(), e.getDefaultMessage()));
    problemDetail.setProperty("errors", errorMap);
    return ResponseEntity.status(status).body(problemDetail);
  }

  // 예상 못한 에외
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnExcepted(Exception e) {
    log.error("Unhandled Exception message={}", e.getMessage(), e);
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
  }
}
