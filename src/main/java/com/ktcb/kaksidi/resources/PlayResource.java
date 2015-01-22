package com.ktcb.kaksidi.resources;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.annotation.Timed;
import com.ktcb.kaksidi.ChannelMetadataResponseFactory;
import com.ktcb.kaksidi.KaksidiConfiguration;
import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import com.ktcb.kaksidi.core.Play;
import com.ktcb.kaksidi.views.PlayView;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;


@Path("/play")
@Produces(MediaType.APPLICATION_JSON)
public class PlayResource {
    private final AtomicLong counter_JSON;
    private final AtomicLong counter_HTML;
    private final Client elasticSearchClient;
    final static Logger logger = (Logger) LoggerFactory.getLogger(PlayResource.class);

    public PlayResource(Client elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
        this.counter_JSON = new AtomicLong();
        this.counter_HTML = new AtomicLong();
    }

    @GET @Path("{id}/pretty")
    @Produces(MediaType.TEXT_HTML)
    @Timed
    public PlayView getPlayPretty(@PathParam("id") String id) {
        counter_HTML.incrementAndGet();
        Play play = getPlayObject(id);
        return new PlayView(play);
    }

    @GET @Path("{id}")
    @Timed
    public Play getPlay(@PathParam("id") String id) {
        counter_JSON.incrementAndGet();
        Play play = getPlayObject(id);
        return play;
    }

    private Play getPlayObject(String id) {
        String indexName = elasticSearchClient.settings().get(KaksidiConfiguration.Constants.INDEX_NAME_NAME.value);
        GetResponse response = elasticSearchClient.prepareGet(indexName, KaksidiConfiguration.Constants.ES_TYPE.value, id).execute().actionGet();
        JSONObject jsonObject = new JSONObject(response.getSource());
        ChannelMetadataResponse channelMetadataResponse = new ChannelMetadataResponseFactory().build(jsonObject);
        return new Play(response.getId(), channelMetadataResponse.getArtist(), channelMetadataResponse.getTitle(), channelMetadataResponse.getWhen(), channelMetadataResponse.getChannelKey());
    }

}
