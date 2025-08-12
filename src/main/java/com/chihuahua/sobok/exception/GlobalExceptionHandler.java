package com.chihuahua.sobok.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "인증 실패: " + ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "요청 오류: " + ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "서버 내부 오류: " + ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}

