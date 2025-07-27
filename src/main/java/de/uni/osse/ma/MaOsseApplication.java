package de.uni.osse.ma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MaOsseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaOsseApplication.class, args);
    }

}
