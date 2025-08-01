package com.taste;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.taste.mapper")
@SpringBootApplication
public class TasteHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasteHubApplication.class, args);
    }

}
