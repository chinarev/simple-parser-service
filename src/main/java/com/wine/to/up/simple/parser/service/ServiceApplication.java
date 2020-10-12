package com.wine.to.up.simple.parser.service;

import com.wine.to.up.simple.parser.service.SimpleParser.Parser;

import io.micrometer.core.instrument.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan("com.wine.to.up")
@EnableSwagger2
@Slf4j
@EnableScheduling

public class ServiceApplication  {

    private final Parser parser;

    public ServiceApplication(Parser parser){
        this.parser = parser;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @Scheduled(fixedDelayString = "PT12H") //run once in 12 hours
    void scheduledRunParser(){
        log.info("SCHEDULED PARSER START");
        parser.startParser();
    }

}
