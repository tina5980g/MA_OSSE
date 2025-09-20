package de.uni.osse.ma;

import de.uni.osse.ma.config.SystemConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(SystemConfiguration.class)
public class MaOsseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaOsseApplication.class, args);
    }

}
