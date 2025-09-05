package com.beyond.ordersystem.common.service;

import com.beyond.ordersystem.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

// controller 어노테이션이 붙어있는 클래스의 모든 예외를 모니터링하여 예외를 인터셉팅
@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalException(IllegalArgumentException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(CommonErrorDto.builder().status_code(HttpStatus.BAD_REQUEST.value()).status_message(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> noSuchException(NoSuchElementException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(CommonErrorDto.builder().status_code(HttpStatus.NOT_FOUND.value()).status_message(e.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFoundException(EntityNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(CommonErrorDto.builder().status_code(HttpStatus.NOT_FOUND.value()).status_message(e.getMessage()).build(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error(errorMessage);
        return new ResponseEntity<>(CommonErrorDto.builder().status_code(HttpStatus.NOT_FOUND.value()).status_message(e.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.FORBIDDEN.value(),  e.getMessage()), HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
