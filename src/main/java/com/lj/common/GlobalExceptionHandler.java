package com.lj.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {Controller.class, RestController.class})
@ResponseBody
@Slf4j
/**
 * 全局异常处理器
 */
public class GlobalExceptionHandler {
    /**
     * SQL异常处理器(索引字段重复插入)
     * @param ex
     * @return
     */
    @ExceptionHandler
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //判断当前的的异常是否为预期要处理的异常
        if(ex.getMessage().contains("Duplicate entry")){
            String[] message = ex.getMessage().split(" ");
            String msg = message[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }
    
    /**
     * 业务异常处理器
     * @param ex
     * @return
     */
    @ExceptionHandler
    public R<String> exceptionHandler(CustomerException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
