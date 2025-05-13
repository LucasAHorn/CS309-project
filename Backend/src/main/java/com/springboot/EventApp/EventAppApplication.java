package com.springboot.EventApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This class runs the entire application
 *
 * @author Lucas Horn
 */
@SpringBootApplication
@EnableScheduling
public class EventAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventAppApplication.class, args);
    }
}