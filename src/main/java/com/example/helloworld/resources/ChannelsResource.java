package com.example.helloworld.resources;

import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Channels;
import com.example.helloworld.core.Saying;
import com.google.common.base.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelsResource {
    private final AtomicLong counter;
    private final HttpClient httpClient;

    public ChannelsResource(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Channels getChannels() {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        String[] channels = new String[1];
        try {
            HttpGet request = new HttpGet("https://www.siriusxm.com/userservices/cl/en-us/json/lineup/250/client/ump");
            String responseBody = httpclient.execute(request, new ResponseHandler<String>() {
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
            channels[0] = "the response was " + responseBody;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Channels(counter.incrementAndGet(), channels);
    }
}
