package com.backend.amealia.exception;

import com.backend.amealia.constants.Constants;
import com.backend.amealia.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.backend.amealia.constants.Constants.INVALID_DATA_MESSAGE;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleGenericException(Exception ex) {
        log.info("An unhandled exception occurred, see error -> {}", ex.getMessage(), ex);
        return ApiResponse.failure(Constants.MESSAGE_INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResourceFoundException(NoHandlerFoundException ex) {
        return ApiResponse.failure("Route not found " + ex.getRequestURL());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("%s is not not supported for this endpoint. Supported methods: %s", ex.getMethod(), Arrays.toString(ex.getSupportedMethods()));
        return ApiResponse.failure(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String,String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMessage = error.getDefaultMessage();

            validationErrors.put(fieldName, validationMessage);
        });

        return ApiResponse.failure(INVALID_DATA_MESSAGE, validationErrors);
    }
}
