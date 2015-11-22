package com.herzog.api;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application configuration
 */
@Data
@NoArgsConstructor
public class IdentificationConfiguration extends Configuration {
    @JsonProperty
    private String template;
    @JsonProperty
    private String defaultName = "Stranger";
}
