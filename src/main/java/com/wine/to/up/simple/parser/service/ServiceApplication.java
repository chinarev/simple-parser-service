package com.wine.to.up.simple.parser.service;

import com.wine.to.up.simple.parser.service.SimpleParser.Parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan("com.wine.to.up")
@EnableSwagger2
@Slf4j
public class ServiceApplication implements CommandLineRunner {

    private final Parser parser;

    public ServiceApplication(Parser parser){
        this.parser = parser;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        parser.startParser();
    }
}
