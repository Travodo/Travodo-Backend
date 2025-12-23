package gdg.travodobackend.global.exception;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("message", "입력값 검증에 실패했습니다");
        response.put("data", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 오류 발생", e);
        
        Map<String, Object> response = new HashMap<>();
        String message = "요청 본문의 JSON 형식이 올바르지 않습니다";
        
        // 더 구체적인 에러 메시지 추출
        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException) {
            JsonParseException jsonException = (JsonParseException) cause;
            message = String.format("JSON 파싱 오류: %s (위치: %d번째 줄, %d번째 컬럼)", 
                    jsonException.getOriginalMessage(),
                    jsonException.getLocation().getLineNr(),
                    jsonException.getLocation().getColumnNr());
        }
        
        response.put("message", message);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("필수 파라미터가 누락되었습니다: %s", e.getParameterName()));
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("파라미터 값이 올바르지 않습니다: %s", e.getName()));
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<Map<String, Object>> handleKakaoApiException(KakaoApiException e) {
        log.error("카카오 API 오류 발생: {}", e.getMessage(), e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        
        int httpStatus = e.getHttpStatus();
        response.put("status", httpStatus);
        
        HttpStatus status = HttpStatus.resolve(httpStatus);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(AccountLinkRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLinkRequiredException(AccountLinkRequiredException e) {
        log.info("계정 통합 필요: 기존 제공자={}, 이메일={}", e.getExistingProvider(), e.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("needsConfirmation", true);
        response.put("message", e.getMessage());
        response.put("existingProvider", e.getExistingProvider().name());
        response.put("email", e.getEmail());
        response.put("status", e.getHttpStatus());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(ForbiddenException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "서버 오류가 발생했습니다");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

