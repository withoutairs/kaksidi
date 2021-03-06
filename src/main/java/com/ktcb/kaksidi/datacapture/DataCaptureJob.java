package com.ktcb.kaksidi.datacapture;

import ch.qos.logback.classic.Logger;
import com.ktcb.kaksidi.ChannelMetadataResponseFactory;
import com.ktcb.kaksidi.KaksidiConfiguration;
import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataCaptureJob implements Runnable {
    public static final String SXM_TIMESTAMP_PATTERN = "MM-dd'-'HH:mm:'00'"; // The hardcoded 00 are corny, but it looks like that's the way SXM likes it
    private final String channel;
    private final HttpClient httpClient;
    private final Client elasticSearchClient;
    private final String sxmTimestampUri = "https://www.siriusxm.com/metadata/pdt/en-us/json/channels/%s/timestamp/%s?%s";
    private final DataCaptureJobConfiguration dataCaptureJobConfiguration;
    private final List<StorageStrategy> storageStrategies;

    public DataCaptureJob(String channel, HttpClient httpClient, Client elasticSearchClient, DataCaptureJobConfiguration dataCaptureJobConfiguration) {
        this.channel = channel;
        this.httpClient = httpClient;
        this.elasticSearchClient = elasticSearchClient;
        this.dataCaptureJobConfiguration = dataCaptureJobConfiguration;
        this.storageStrategies = new ArrayList<StorageStrategy>();
        final ZipStorageStrategy zipStorageStrategy = new ZipStorageStrategy(dataCaptureJobConfiguration.getZipStorageDirectory());
        this.storageStrategies.add(zipStorageStrategy); // TODO config?
        this.storageStrategies.add(new TempFileStorageStrategy()); // TODO config?
    }

    @Override
    public void run() {
        // TODO splay
        final Logger logger = (Logger) LoggerFactory.getLogger(DataCaptureJob.class + "-" + channel);
        String indexName = elasticSearchClient.settings().get(KaksidiConfiguration.Constants.INDEX_NAME_NAME.value);

        try {
            int sampleFrequency = dataCaptureJobConfiguration.getSampleFrequencySeconds();
            FilteredQueryBuilder queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                    FilterBuilders.andFilter(FilterBuilders.termFilter("channelId", channel),
                            FilterBuilders.rangeFilter("_timestamp").from("now-" + sampleFrequency + "s")
                                    .to("now")
                                    .includeLower(false)
                                    .includeUpper(true)));
            logger.debug(queryBuilder.buildAsBytes().toUtf8());
            SearchResponse searchResponse = elasticSearchClient.prepareSearch(indexName).
                    setTypes(KaksidiConfiguration.Constants.ES_TYPE.value).
                    setQuery(queryBuilder)
                    .get();
            long totalHits = searchResponse.getHits().getTotalHits();
            logger.info("Found " + totalHits + " in the last " + sampleFrequency + " seconds.");
            if (totalHits > 0) return;

            LocalDateTime nowInUtc = LocalDateTime.now(Clock.systemUTC());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(SXM_TIMESTAMP_PATTERN);
            String timestamp = nowInUtc.format(dateTimeFormatter);
            final int cacheBuster = new Random().nextInt();
            final String uri = String.format(sxmTimestampUri, channel, timestamp, cacheBuster);
            logger.debug("Calling SXM to capture data from " + uri);

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

            {
                JSONObject jsonObject = new JSONObject(responseBody);
                ChannelMetadataResponse channelMetadataResponse = new ChannelMetadataResponseFactory().build(jsonObject);
                if (channelMetadataResponse.equals(ChannelMetadataResponse.NULL)) {
                    logger.warn("Content unavailable from " + uri);
                    return;
                }

                final String code = channelMetadataResponse.getCode();
                if (code.equals("100")) {
                    storageStrategies.forEach((strategy) -> strategy.apply(responseBody));

                    IndexResponse indexResponse = elasticSearchClient.prepareIndex(indexName, KaksidiConfiguration.Constants.ES_TYPE.value).setSource(responseBody).execute().actionGet();
                    logger.info("Successful, added ES_ID=" + indexResponse.getId() + " from " + channelMetadataResponse + " should be at http://localhost:9200/" + indexResponse.getIndex() + "/" + indexResponse.getType() + "/" + indexResponse.getId());
                } else {
                    logger.warn("Failed, response was " + responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Failed for channel=" + channel, e);
        }
    }
}
