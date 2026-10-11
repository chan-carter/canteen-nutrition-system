package com.health.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CanteenServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanteenServerApplication.class, args);
    }

}
