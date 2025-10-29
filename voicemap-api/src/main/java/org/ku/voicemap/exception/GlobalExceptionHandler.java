package org.ku.voicemap.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {

    log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(e);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }

  @ExceptionHandler(VoiceMapException.class)
  public ResponseEntity<ErrorResponse> handleOotdException(VoiceMapException e) {

    log.error("커스텀 예외 발생: message={}", e.getMessage(), e);
    HttpStatus status = determineHttpStatus(e);
    ErrorResponse response = new ErrorResponse(e);

    return ResponseEntity
        .status(status)
        .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e) {

    log.error("요청 유효성 검사 실패: {}", e.getMessage());

    Map<String, Object> validationErrors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    ErrorResponse response = new ErrorResponse(
        "VALIDATION_ERROR",
        "요청 데이터 유효성 검사에 실패했습니다.",
        validationErrors
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  private HttpStatus determineHttpStatus(
      VoiceMapException e) {
    ErrorCode errorCode = e.getErrorCode();
    return switch (errorCode) {
      case AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
      case MEMBER_EXIST_REGISTER, MEMBER_NOT_FOUND -> HttpStatus.CONFLICT;
    };
  }


}
