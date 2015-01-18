package com.ktcb.kaksidi.core;

import java.time.OffsetDateTime;

/**
 * Intended to hold data from from Sirius XM.
 * <p>
 * The call we are making to them contains packets of "channelMetadataResponse", thus this class' name.
 */
public class ChannelMetadataResponse {
    private String siriusXmId;
    private String artist;
    private String title;
    private OffsetDateTime when;
    private String channelKey;
    private String code;

    public ChannelMetadataResponse(String siriusXmId, String artist, String title, OffsetDateTime when, String channelKey, String code) {
        this.siriusXmId = siriusXmId;
        this.artist = artist;
        this.title = title;
        this.when = when;
        this.channelKey = channelKey;
        this.code = code;
    }

    public String getSiriusXmId() {
        return siriusXmId;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getWhen() {
        return when;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static ChannelMetadataResponse NULL = new ChannelMetadataResponse("NULL", "Not available", "Not available", OffsetDateTime.now(), "Not available", "0");

    @Override
    public String toString() {
        return "ChannelMetadataResponse{" +
                "siriusXmId='" + siriusXmId + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", when=" + when +
                ", channelKey='" + channelKey + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelMetadataResponse)) return false;

        ChannelMetadataResponse that = (ChannelMetadataResponse) o;

        if (!artist.equals(that.artist)) return false;
        if (!channelKey.equals(that.channelKey)) return false;
        if (!code.equals(that.code)) return false;
        if (!siriusXmId.equals(that.siriusXmId)) return false;
        if (!title.equals(that.title)) return false;
        if (!when.equals(that.when)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = siriusXmId.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + when.hashCode();
        result = 31 * result + channelKey.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }
}

