package com.herzog.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Identification {

    @JsonProperty
    private long id;

    @Length(max = 3)
    @JsonProperty
    private String content;
}
