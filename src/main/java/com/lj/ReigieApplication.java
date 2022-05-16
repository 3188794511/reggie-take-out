package com.lj;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ServletComponentScan//组件扫描
@Slf4j
@EnableTransactionManagement//开启事务管理
@EnableScheduling//开启定时任务
public class ReigieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReigieApplication.class);
        log.info("SpringBoot项目启动...");
    }
}
