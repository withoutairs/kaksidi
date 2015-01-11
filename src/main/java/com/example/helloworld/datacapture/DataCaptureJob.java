package com.example.helloworld.datacapture;

import ch.qos.logback.classic.Logger;
import com.example.helloworld.ChannelMetadataResponseFactory;
import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.core.ChannelMetadataResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataCaptureJob implements Runnable {
    public static final String SXM_TIMESTAMP_PATTERN = "MM-dd'-'kk:mm:'00'";     // TODO the hardcoded 00's are so corny but how does SXM work?
    private final String channel;
    private final HttpClient httpClient;
    private final Client elasticSearchClient;
    private final String sxmTimestampUri = "https://www.siriusxm.com/metadata/pdt/en-us/json/channels/%s/timestamp/%s";

    public DataCaptureJob(String channel, HttpClient httpClient, Client elasticSearchClient) {
        this.channel = channel;
        this.httpClient = httpClient;
        this.elasticSearchClient = elasticSearchClient;
    }

    @Override
    public void run() {
        // TODO splay
        final Logger logger = (Logger) LoggerFactory.getLogger(DataCaptureJob.class + "-" + channel);
        try {
            LocalDateTime nowInUtc = LocalDateTime.now(Clock.systemUTC());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(SXM_TIMESTAMP_PATTERN);
            String timestamp = nowInUtc.format(dateTimeFormatter);
            final String uri = String.format(sxmTimestampUri, channel, timestamp);
            logger.info("Calling SXM to capture data from " + uri);

            HttpGet request = new HttpGet(uri);
            String responseBody = httpClient.execute(request, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });

            JSONObject jsonObject = new JSONObject(responseBody);
            ChannelMetadataResponse channelMetadataResponse = new ChannelMetadataResponseFactory().build(jsonObject);
            final String code = channelMetadataResponse.getCode();
            if (code.equals("100")) {
                String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
                IndexResponse indexResponse = elasticSearchClient.prepareIndex(indexName, HelloWorldConfiguration.Constants.ES_TYPE.value).setSource(responseBody).execute().actionGet();
                logger.info("Successful, added ES_ID=" + indexResponse.getId() + " from " + channelMetadataResponse + " should be at http://localhost:9200/" + indexResponse.getIndex() + "/" + indexResponse.getType() + "/" + indexResponse.getId());
            } else if (code.equals("305")) {
                // I suspect the timestamp being out of sync with SXM's expectations causes these
                logger.warn("Content unavailable, timestamp=" + timestamp);
            } else {
                logger.warn("Failed, response was " + responseBody);
            }
        } catch (Exception e) {
            logger.error("Failed for channel=" + channel, e);
        }
    }
}
