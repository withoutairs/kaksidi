package com.example.helloworld.resources;

import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Play;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;


@Path("/play")
@Produces(MediaType.APPLICATION_JSON)
public class PlayResource {
    public static final String ELASTICSEARCH_INDEX = "xm"; // TODO this doesn't belong here
    public static final String ELASTICSEARCH_MAPPING = "timestamp"; // TODO this doesn't belong here
    private final AtomicLong counter;
    private final Client elasticSearchClient;

    public PlayResource(Client elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
        this.counter = new AtomicLong();
    }

    @GET @Path("{id}")
    @Timed
    public Play getPlay(@PathParam("id") String id) {
        counter.incrementAndGet();
        ISO8601DateFormat df = new ISO8601DateFormat();
        GetResponse response = elasticSearchClient.prepareGet(ELASTICSEARCH_INDEX, ELASTICSEARCH_MAPPING, id).execute().actionGet();
        JSONObject jsonObject = new JSONObject(response.getSource());
        JSONObject channelMetadataResponse = jsonObject.getJSONObject("channelMetadataResponse");
        JSONObject metaData = channelMetadataResponse.getJSONObject("metaData");
        String channelId = metaData.get("channelId").toString(); // TODO channelKey in the ChannelResource, unsure this is the samme
        Date when;
        try {
            when  = df.parse(metaData.get("dateTime").toString());
        } catch (ParseException e) {
            // TODO log it
            when = new Date();
        }
        JSONObject currentEvent = metaData.getJSONObject("currentEvent");
        JSONObject song = currentEvent.getJSONObject("song");
        String name = song.get("name").toString();
        JSONObject artists = currentEvent.getJSONObject("artists");
        String artist = artists.get("name").toString();
        return new Play(response.getId(), artist, name, when, channelId);
    }
}
