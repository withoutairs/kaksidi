package com.example.helloworld.resources;
import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Play;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
        GetResponse response = elasticSearchClient.prepareGet(ELASTICSEARCH_INDEX, ELASTICSEARCH_MAPPING, id).execute().actionGet();
        String artist = response.getField("channelMetadataResponse").toString(); // TODO not really
        return new Play(response.getId(), artist, "", new Date(), "");
    }
}
