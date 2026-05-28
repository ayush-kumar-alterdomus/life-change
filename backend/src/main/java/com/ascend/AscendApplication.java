package com.ascend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AscendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AscendApplication.class, args);
    }
}
