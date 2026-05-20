package com.bug.catcher.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직(Service)에서 던지는 IllegalArgumentException을 잡아서
     * 자바의 복잡한 에러 로그(JSON) 대신, 깔끔한 에러 메시지 텍스트만 프론트엔드로 반환합니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // 400 Bad Request 상태 코드와 함께 에러 메시지만 딱 보냅니다.
        // 예: "정지된 계정입니다. (정지 해제 일시: ...)"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * 혹시 모를 다른 모든 예외들도 깔끔하게 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
