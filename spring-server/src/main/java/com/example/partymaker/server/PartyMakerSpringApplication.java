package com.example.partymaker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * מחלקה ראשית של שרת PartyMaker מבוסס Spring Boot
 */
@SpringBootApplication
@EnableScheduling
public class PartyMakerSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartyMakerSpringApplication.class, args);
    }
} 