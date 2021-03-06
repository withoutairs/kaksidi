package com.ktcb.kaksidi;

import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class ChannelMetadataResponseFactory {
    final static Logger logger = (Logger) LoggerFactory.getLogger(ChannelMetadataResponseFactory.class);

    public ChannelMetadataResponse build(JSONObject jsonObject) {
        JSONObject channelMetadataResponse = jsonObject.getJSONObject("channelMetadataResponse");
        JSONObject messages = channelMetadataResponse.getJSONObject("messages");
        String code = messages.get("code").toString();
        if (code.equals("305")) {
            return ChannelMetadataResponse.NULL;
        }

        JSONObject metaData;
        try {
            metaData = channelMetadataResponse.getJSONObject("metaData");
        } catch (org.json.JSONException e) {
            logger.error("No metadata element found in " + jsonObject.toString());
            return ChannelMetadataResponse.NULL;
        }
        String channelId = metaData.get("channelId").toString(); // TODO channelKey in the ChannelResource, unsure this is the same
        JSONObject currentEvent = metaData.getJSONObject("currentEvent");
        String siriusXmId = currentEvent.get("siriusXMId").toString();
        JSONObject song = currentEvent.getJSONObject("song");
        String name = song.get("name").toString();
        JSONObject artists = currentEvent.getJSONObject("artists");
        String artist = artists.get("name").toString();

        String startTime = currentEvent.get("startTime").toString();
        OffsetDateTime when;
        try {
            when = OffsetDateTime.parse(startTime);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse OffsetDateTime, startTime=" + startTime + ", siriusXmId=" + siriusXmId, e);
            when = OffsetDateTime.now();
        }

        return new ChannelMetadataResponse(siriusXmId, artist, name, when, channelId, code);
    }
}
