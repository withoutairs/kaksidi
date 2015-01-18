package com.ktcb.kaksidi.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

public class Channels {
    private long id;

    @Length(max = 3)
    private List<String> content;

    public Channels() {
        // Jackson deserialization
    }

    public Channels(long id, List<String> content) {
        this.id = id;
        this.content = content;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public List<String> getContent() {
        return content;
    }
}
