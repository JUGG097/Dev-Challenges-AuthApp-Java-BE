package com.example.base.error;

import com.example.base.entity.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AuthAppException.class)
    public ResponseEntity<ErrorResponse> authAppException(AuthAppException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.builder()
                .message(e.getMessage())
                .success(false)
                .build());
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> tokenRefreshException(TokenRefreshException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .message(e.getMessage())
                .success(false)
                .build());
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> userServiceException(UserServiceException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .message(e.getMessage())
                .success(false)
                .build());
    }
}
