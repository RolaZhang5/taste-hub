package com.taste.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        //Configure
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
//        config.useSingleServer().setAddress("rediss://default:AWe_AAIjcDEyZjc0MzRkNDE4YTY0ODU4ODg5YmExMWY4NmU5MzQ4YnAxMA@learning-glider-26559.upstash.io:6379");
        //Create RedissonClient class
        return Redisson.create(config);
    }
}
