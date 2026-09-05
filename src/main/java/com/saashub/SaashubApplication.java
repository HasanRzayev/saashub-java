package com.saashub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SaashubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaashubApplication.class, args);
    }
}
