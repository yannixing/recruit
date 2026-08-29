package com.recruit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 招聘管理系统启动类。
 */
@Slf4j
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.recruit.mapper")
@SpringBootApplication
public class RecruitApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitApplication.class, args);
        log.info("招聘管理系统启动成功");
    }
}
