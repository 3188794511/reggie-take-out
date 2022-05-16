package com.lj.common;

/**
 * 每次客户端发起请求时,服务器都会创建一个新的线程去处理请求,而一次请求都是由同一个线程来处理的
 * 获取当前线程的ThreadLocal,相当于当前线程的一个局部变量,可以存储数据
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();//用来存储登录员工的id
    
    /**
     * 设置值
     * @param id
     */
    public static void setCurrenId(Long id){
        threadLocal.set(id);
    }
    
    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
