package de.uni.osse.ma.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paths")
@Data
public class SystemConfiguration {

    private String pythonExecutable;
    private String datasetStorageDir;
}
