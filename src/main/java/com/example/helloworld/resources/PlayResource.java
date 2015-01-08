package com.example.helloworld.resources;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Play;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicLong;


@Path("/play")
@Produces(MediaType.APPLICATION_JSON)
public class PlayResource {
    public static final String ELASTICSEARCH_INDEX = "xm"; // TODO this doesn't belong here
    public static final String ELASTICSEARCH_MAPPING = "timestamp"; // TODO this doesn't belong here
    private final AtomicLong counter;
    private final Client elasticSearchClient;
    final static Logger logger = (Logger) LoggerFactory.getLogger(PlayResource.class);

    public PlayResource(Client elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
        this.counter = new AtomicLong();
    }

    @GET @Path("{id}")
    @Timed
    public Play getPlay(@PathParam("id") String id) {
        counter.incrementAndGet();
        GetResponse response = elasticSearchClient.prepareGet(ELASTICSEARCH_INDEX, ELASTICSEARCH_MAPPING, id).execute().actionGet();
        JSONObject jsonObject = new JSONObject(response.getSource());
        JSONObject channelMetadataResponse = jsonObject.getJSONObject("channelMetadataResponse");
        JSONObject metaData = channelMetadataResponse.getJSONObject("metaData");
        String channelId = metaData.get("channelId").toString(); // TODO channelKey in the ChannelResource, unsure this is the samme
        String dateTime = metaData.get("dateTime").toString();
        OffsetDateTime when;
        try {
            when  = OffsetDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse OffsetDateTime, dateTime=" + dateTime + ", playId = " + id, e);
            when = OffsetDateTime.now();
        }
        JSONObject currentEvent = metaData.getJSONObject("currentEvent");
        JSONObject song = currentEvent.getJSONObject("song");
        String name = song.get("name").toString();
        JSONObject artists = currentEvent.getJSONObject("artists");
        String artist = artists.get("name").toString();
        return new Play(response.getId(), artist, name, when, channelId);
    }
}
