package com.example.partymaker.server.exception;

import com.example.partymaker.server.model.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * מטפל חריגות גלובלי לכל הבקרים
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * מטפל בחריגות כלליות
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception occurred for request: {}", request.getRequestURI(), ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Internal server error", 
                ex);
        
        return buildResponseEntity(apiError);
    }
    
    /**
     * מטפל בחריגות של גודל קובץ חורג ממגבלה
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.warn("File size limit exceeded for request: {}", request.getRequestURI(), ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.PAYLOAD_TOO_LARGE, 
                "File size exceeds the maximum allowed limit", 
                ex);
        
        return buildResponseEntity(apiError);
    }
    
    /**
     * מטפל בחריגות של משאב לא נמצא
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found for request: {}", request.getRequestURI(), ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND, 
                ex.getMessage(), 
                ex);
        
        return buildResponseEntity(apiError);
    }
    
    /**
     * מטפל בחריגות של הודעה לא קריאה
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        logger.warn("Malformed JSON request", ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST, 
                "Malformed JSON request", 
                ex);
        
        return buildResponseEntity(apiError);
    }
    
    /**
     * מטפל בחריגות של ארגומנטים לא תקינים
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        logger.warn("Validation error", ex);
        
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error");
        
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> 
            apiError.addError(fieldError.getField() + ": " + fieldError.getDefaultMessage())
        );
        
        return buildResponseEntity(apiError);
    }
    
    /**
     * מטפל בחריגות של נתיב לא נמצא
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()),
                ex);
        
        return buildResponseEntity(apiError);
    }
    
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
} 