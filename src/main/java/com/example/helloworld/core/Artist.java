package com.example.helloworld.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Artist {
    private String name;
    public Artist(String name) { this.name = name; }

    @JsonProperty
    public String getName() {
        return name;
    }


    public static Artist NULL = new Artist("Not Available");
}
