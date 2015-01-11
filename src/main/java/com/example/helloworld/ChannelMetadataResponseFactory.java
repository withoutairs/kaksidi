package com.example.helloworld;

import ch.qos.logback.classic.Logger;
import com.example.helloworld.core.ChannelMetadataResponse;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class ChannelMetadataResponseFactory {
    final static Logger logger = (Logger) LoggerFactory.getLogger(ChannelMetadataResponseFactory.class);

    public ChannelMetadataResponse build(JSONObject jsonObject) {
        JSONObject channelMetadataResponse = jsonObject.getJSONObject("channelMetadataResponse");
        JSONObject metaData = channelMetadataResponse.getJSONObject("metaData");
        String channelId = metaData.get("channelId").toString(); // TODO channelKey in the ChannelResource, unsure this is the same
        JSONObject currentEvent = metaData.getJSONObject("currentEvent");
        String siriusXmId = currentEvent.get("siriusXMId").toString();
        JSONObject song = currentEvent.getJSONObject("song");
        String name = song.get("name").toString();
        JSONObject artists = currentEvent.getJSONObject("artists");
        String artist = artists.get("name").toString();

        JSONObject messages = channelMetadataResponse.getJSONObject("messages");
        String code = messages.get("code").toString();

        String dateTime = metaData.get("dateTime").toString();
        OffsetDateTime when;
        try {
            when = OffsetDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse OffsetDateTime, dateTime=" + dateTime + ", siriusXmId = " + siriusXmId, e);
            when = OffsetDateTime.now();
        }

        return new ChannelMetadataResponse(siriusXmId, artist, name, when, channelId, code);
    }
}
