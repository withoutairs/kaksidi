package com.example.helloworld.resources;
import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Play;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;


@Path("/play")
@Produces(MediaType.APPLICATION_JSON)
public class PlayResource {
    private final AtomicLong counter;
    private final HttpClient httpClient;

    public PlayResource(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Play getPlay() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet("STUFF");
            String responseBody = null;
            try {
                responseBody = httpclient.execute(request, new ResponseHandler<String>() {
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
                JSONObject jsonObject = new JSONObject(responseBody); // TODO etc
                return Play.NULL;
            } catch (IOException e) {
                e.printStackTrace();
                return Play.NULL;
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
