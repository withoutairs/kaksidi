package com.ktcb.kaksidi.resources;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.annotation.Timed;
import com.ktcb.kaksidi.KaksidiConfiguration;
import com.ktcb.kaksidi.core.Play;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Path("/artist")
@Produces(MediaType.APPLICATION_JSON)
public class ArtistResource {
    private final AtomicLong counter;
    private final Client elasticSearchClient;
    final static Logger logger = (Logger) LoggerFactory.getLogger(ArtistResource.class);

    public ArtistResource(Client elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
        this.counter = new AtomicLong();
    }

    @GET
    @Path("{name}/plays")
    @Timed
    public List<Play> getPlays(@PathParam("name") String name) {
        counter.incrementAndGet();
        List<Play> plays = new ArrayList<Play>();
        try {
            ISO8601DateFormat df = new ISO8601DateFormat();
            String indexName = elasticSearchClient.settings().get(KaksidiConfiguration.Constants.INDEX_NAME_NAME.value);
            SearchResponse response = elasticSearchClient.prepareSearch(indexName).setSearchType(SearchType.QUERY_AND_FETCH).setTypes(KaksidiConfiguration.Constants.ES_TYPE.value).setQuery(QueryBuilders.multiMatchQuery(name, "name")).execute().actionGet(); // TODO "name" is too generic, need to match only the artist name
            SearchHits hits = response.getHits();
            for (Iterator<SearchHit> iterator = hits.iterator(); iterator.hasNext(); ) {
                SearchHit hit = iterator.next();
                JSONObject jsonObject = new JSONObject(hit.getSource());
                JSONObject channelMetadataResponse = jsonObject.getJSONObject("channelMetadataResponse");
                JSONObject metaData = channelMetadataResponse.getJSONObject("metaData");
                String channelId = metaData.get("channelId").toString(); // TODO channelKey in the ChannelResource, unsure this is the samme

                JSONObject currentEvent = metaData.getJSONObject("currentEvent");
                JSONObject song = currentEvent.getJSONObject("song");
                String songName = song.get("name").toString();

                OffsetDateTime when = OffsetDateTime.parse(currentEvent.get("startTime").toString());

                JSONObject artists = currentEvent.getJSONObject("artists");
                String artist = artists.get("name").toString();

                plays.add(new Play(hit.getId(), artist, songName, when, channelId));
            }
        } catch (Exception e) {
            logger.error("Could not handle getPlays, artist = " + name, e);
            if (plays.isEmpty()) {
                plays.add(Play.NULL);
            }
        }
        return plays;
    }
}
