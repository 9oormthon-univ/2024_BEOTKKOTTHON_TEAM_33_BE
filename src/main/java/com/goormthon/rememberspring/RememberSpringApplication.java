package com.goormthon.rememberspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RememberSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(RememberSpringApplication.class, args);
    }

}
