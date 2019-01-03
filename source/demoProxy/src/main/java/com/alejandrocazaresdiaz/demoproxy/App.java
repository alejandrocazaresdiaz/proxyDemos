package com.alejandrocazaresdiaz.demoproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author Alejandro Cazares
 */
@Configuration
@ComponentScan
@SpringBootApplication
@EnableScheduling
public class App {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(App.class, args);
    }
}
