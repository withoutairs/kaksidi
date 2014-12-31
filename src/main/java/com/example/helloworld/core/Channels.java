package com.example.helloworld.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

public class Channels {
    private long id;

    @Length(max = 3)
    private String[] content;

    public Channels() {
        // Jackson deserialization
    }

    public Channels(long id, String[] content) {
        this.id = id;
        this.content = content;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String[] getContent() {
        return content;
    }
}
