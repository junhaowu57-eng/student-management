package com.example.student_management.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.example.student_management.common.Result;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e){
        String message=e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.error(400,message);
    }
}
