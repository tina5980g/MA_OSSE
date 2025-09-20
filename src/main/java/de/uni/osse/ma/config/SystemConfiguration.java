package de.uni.osse.ma.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "paths")
@Data
public class SystemConfiguration {

    @NotBlank
    private String pythonExecutable;
    private String datasetStorageDir;
}
