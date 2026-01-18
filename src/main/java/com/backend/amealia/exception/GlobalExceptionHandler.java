package com.backend.amealia.exception;

import com.backend.amealia.constants.Constants;
import com.backend.amealia.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.backend.amealia.constants.Constants.INVALID_DATA_MESSAGE;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.info("An unhandled exception occurred, see error -> {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.failure(Constants.MESSAGE_INTERNAL_SERVER_ERROR);

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String,String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMessage = error.getDefaultMessage();

            validationErrors.put(fieldName, validationMessage);
        });

        ApiResponse<?> response = ApiResponse.failure(INVALID_DATA_MESSAGE, validationErrors);

        return new ResponseEntity<ApiResponse>(response, HttpStatus.BAD_REQUEST);
    }
}
