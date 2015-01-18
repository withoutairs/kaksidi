package com.ktcb.kaksidi;

import ch.qos.logback.classic.Logger;
import com.ktcb.kaksidi.datacapture.DataCaptureJob;
import com.ktcb.kaksidi.datacapture.DataCaptureJobConfiguration;
import com.ktcb.kaksidi.resources.ArtistResource;
import com.ktcb.kaksidi.resources.ChannelsResource;
import com.ktcb.kaksidi.resources.PlayResource;
import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.http.client.HttpClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class KaksidiApplication extends Application<KaksidiConfiguration> {
    final Logger logger = (Logger) LoggerFactory.getLogger(KaksidiApplication.class);

    public static void main(String[] args) throws Exception {
        new KaksidiApplication().run(args);
    }

    @Override
    public String getName() {
        return "kaksidi";
    }

    @Override
    public void initialize(Bootstrap<KaksidiConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(KaksidiConfiguration configuration,
                    Environment environment) {

        final Client elasticSearchClient = configuration.getElasticSearchClientFactory().build(environment);
        getOrCreateIndex(elasticSearchClient);

        String indexName = elasticSearchClient.settings().get(KaksidiConfiguration.Constants.INDEX_NAME_NAME.value);
        final HttpClient httpClient = new HttpClientBuilder(environment).using(configuration.getHttpClientConfiguration()).build(indexName);
        final ChannelsResource channelsResource = new ChannelsResource(httpClient);
        environment.jersey().register(channelsResource);

        final PlayResource playResource = new PlayResource(elasticSearchClient);
        environment.jersey().register(playResource);

        final ArtistResource artistResource = new ArtistResource(elasticSearchClient);
        environment.jersey().register(artistResource);

        final DataCaptureJobConfiguration dataCaptureJobConfiguration = configuration.getDataCaptureJobConfiguration();
        if (dataCaptureJobConfiguration.isEnabled()) {

            final int unhealthyThresholdSeconds = dataCaptureJobConfiguration.getUnhealthyThresholdSeconds();
            final DataCaptureHealthCheck dataCaptureHealthCheck = new DataCaptureHealthCheck(elasticSearchClient, unhealthyThresholdSeconds);
            environment.healthChecks().register("datacapture", dataCaptureHealthCheck);
            environment.jersey().register(dataCaptureHealthCheck);

            String[] channels = configuration.getChannels(); // TODO pull from ChannelResource
            for (int i = 0; i < channels.length; i++) {
                String channel = channels[i];
                ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(channel);
                ScheduledExecutorService ses = sesBuilder.build();
                Runnable alarmTask = new DataCaptureJob(channel, httpClient, elasticSearchClient, dataCaptureJobConfiguration);
                int attemptFrequencySeconds = dataCaptureJobConfiguration.getAttemptFrequencySeconds();
                ses.scheduleWithFixedDelay(alarmTask, 0, attemptFrequencySeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void getOrCreateIndex(Client elasticSearchClient) {
        String indexName = elasticSearchClient.settings().get(KaksidiConfiguration.Constants.INDEX_NAME_NAME.value);
        IndicesExistsResponse indicesExistsResponse = elasticSearchClient.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (!indicesExistsResponse.isExists()) {
            final XContentBuilder mappingBuilder;
            try {
                String documentType = KaksidiConfiguration.Constants.ES_TYPE.value;
                mappingBuilder = jsonBuilder().startObject().
                        startObject(documentType)
                        .startObject("_timestamp").field("enabled", "true").field("store", "true").endObject()
                        .endObject()
                        .endObject();
                String json = mappingBuilder.string();
                CreateIndexRequestBuilder createIndexRequestBuilder = elasticSearchClient.admin().indices().prepareCreate(indexName);
                createIndexRequestBuilder.addMapping(documentType, json);
                CreateIndexRequest createIndexRequest = createIndexRequestBuilder.request();
                elasticSearchClient.admin().indices().create(createIndexRequest);
            } catch (IOException e) {
                logger.error("Could not add timestamp to index", e);
            }
        }
    }
}