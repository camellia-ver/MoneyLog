package com.MoneyLog.exception;

import com.MoneyLog.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
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
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "인증이 만료되었거나 존재하지 않는 사용자입니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "필수 파라미터가 누락되었습니다: " + e.getParameterName());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoryNotFound(CategoryNotFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CategoryAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoryAccessDenied(CategoryAccessDeniedException e){
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }


    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleExpenseNotFound(ExpenseNotFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ExpenseAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleExpenseAccessDenied(ExpenseAccessDeniedException e){
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPassword(InvalidPasswordException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CategoryHasExpensesException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoryHasExpenses(CategoryHasExpensesException e){
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception e) {
        log.error("Unhandled exception occurred", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(NoResourceFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.");
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
