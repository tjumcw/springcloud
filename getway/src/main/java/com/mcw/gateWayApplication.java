package com.mcw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author 缪长蔚
 * @create 2024/1/1
 * @desc
 */

@EnableZuulProxy
@EnableAutoConfiguration
public class gateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(gateWayApplication.class, args);
    }
}
