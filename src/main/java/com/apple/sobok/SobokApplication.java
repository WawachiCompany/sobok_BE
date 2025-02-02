package com.apple.sobok;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SobokApplication {

    public static void main(String[] args) {
        SpringApplication.run(SobokApplication.class, args);
    }

}
