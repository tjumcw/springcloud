package com.mcw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZipkinClient {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinClient.class, args);
    }
}
