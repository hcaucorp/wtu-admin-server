package com.jvmp.vouchershop;

import org.bitcoinj.utils.BriefLogFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Application {

    public static void main(String[] args) {
        BriefLogFormatter.init();

        SpringApplication.run(Application.class, args);
    }
}
