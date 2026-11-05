package com.example.flight.common.exception;

import com.example.flight.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        String msg = "参数校验错误";
        if (e instanceof MethodArgumentNotValidException ex) {
            msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (e instanceof BindException ex) {
            msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        log.warn("参数校验异常: {}", msg);
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error(500, "系统繁忙，请稍后再试: " + e.getMessage());
    }
}
