package com.lj.common;

/**
 * 自定义业务异常类
 */
public class CustomerException extends RuntimeException{
    public CustomerException(String message){
        super(message);
    }
}
