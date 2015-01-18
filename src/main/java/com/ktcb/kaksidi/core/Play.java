package com.ktcb.kaksidi.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class Play {
    private String id; // straight from ES
    private String artist;
    private String title;
    private OffsetDateTime when;
    private String channelKey;

    public Play(String id, String artist, String title, OffsetDateTime when, String channelKey) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.when = when;
        this.channelKey = channelKey;
    }

    @JsonProperty
    public String getId() { return id; }

    @JsonProperty
    public String getArtist() {
        return artist;
    }

    @JsonProperty
    public String getTitle() {
        return title;
    }

    @JsonProperty
    public OffsetDateTime getWhen() {
        return when;
    }

    @JsonProperty
    public String getChannelKey() {
        return channelKey;
    }

    public static Play NULL = new Play("NULL", "Not available", "Not available", OffsetDateTime.now(), "Not available");
}
