package com.example.helloworld.resources;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Channels;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelsResource {
    public static final String SXM_CHANNEL_LIST_URI = "https://www.siriusxm.com/userservices/cl/en-us/json/lineup/250/client/ump";
    private final AtomicLong counter;
    private final HttpClient httpClient;
    final static Logger logger = (Logger) LoggerFactory.getLogger(ChannelsResource.class);

    public ChannelsResource(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Channels getChannels() {

        List<String> channelList = new ArrayList<String>();
        try {
            HttpGet request = new HttpGet(SXM_CHANNEL_LIST_URI);
            String responseBody = httpClient.execute(request, new ResponseHandler<String>() {
                public String handleResponse(final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            });
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONObject lineupResponse = jsonObject.getJSONObject("lineup-response");
            JSONObject lineup = lineupResponse.getJSONObject("lineup");
            JSONArray categories = lineup.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                JSONArray genres = category.getJSONArray("genres");
                for (int j = 0; j < genres.length(); j++) {
                    JSONObject genre = genres.getJSONObject(j);
                    JSONArray channels = genre.getJSONArray("channels");
                    for (int k = 0; k < channels.length(); k++) {
                        JSONObject channel = channels.getJSONObject(k);
                        channelList.add(channel.get("channelKey").toString());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Couldn't get from " + SXM_CHANNEL_LIST_URI, e);
            e.printStackTrace();
        }
        return new Channels(counter.incrementAndGet(), channelList);
    }
}
