package com.herzog.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Application configuration
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class IdentificationConfiguration extends Configuration {

    @JsonProperty
    private String template;

    @JsonProperty
    private String defaultName = "Stranger";

    @JsonProperty
    private String photoBucket;
}
