package com.MoneyLog.exception;

import com.MoneyLog.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmail(DuplicateEmailException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException e){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateCategory(DuplicateCategoryException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException  e){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoryNotFound(CategoryNotFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CategoryAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoryAccessDenied(CategoryAccessDeniedException e){
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String message){
        ErrorResponseDto error = new ErrorResponseDto(
                status.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }
}
