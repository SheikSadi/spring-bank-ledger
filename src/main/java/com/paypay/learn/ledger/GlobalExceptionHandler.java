package com.paypay.learn.ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        AccountNotFoundException ex
    ) {
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Account not found: " + ex.getMessage(),
            List.of() // Empty list
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Catches validation failures caused by @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        List<FieldErrorDetail> fieldErrors = ex
            .getBindingResult().getFieldErrors().stream() // Retrieves the list of validation errors compiled by the jakarta.validation framework
            .map(fe -> new FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
            .toList()
        ;

        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed: " + ex.getMessage(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
        InsufficientFundsException ex
    ) {
        logger.error("Insufficient funds exception caught in global handler", ex);

        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNPROCESSABLE_CONTENT.value(),
            ex.getMessage(),
            List.of()
        );

        return ResponseEntity.unprocessableContent().body(body);
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLogin(
        InvalidLoginException ex
    ) {
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            List.of()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        logger.error("Unhandled exception caught in global handler", ex);

        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occured. Please contact support.",
            List.of()
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
