package com.apple.sobok;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SobokApplication {

    public static void main(String[] args) {
        if(System.getenv("DOCKER_ENV") == null) {
            Dotenv dotenv = Dotenv.configure().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        }
        SpringApplication.run(SobokApplication.class, args);
    }

}
