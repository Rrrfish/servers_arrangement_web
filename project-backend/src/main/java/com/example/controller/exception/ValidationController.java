package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validationException(ValidationException e) {
        return RestBean.fail("请求参数有误");
    }
}
