package com.example.helloworld;

import ch.qos.logback.classic.Logger;
import com.example.helloworld.datacapture.DataCaptureJob;
import com.example.helloworld.resources.ArtistResource;
import com.example.helloworld.resources.ChannelsResource;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.PlayResource;
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

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    final Logger logger = (Logger) LoggerFactory.getLogger(HelloWorldApplication.class);

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {

        final HelloWorldResource resource = new HelloWorldResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(resource);

        final Client elasticSearchClient = configuration.getElasticSearchClientFactory().build(environment);

        getOrCreateIndex(elasticSearchClient);

        final DataCaptureHealthCheck dataCaptureHealthCheck = new DataCaptureHealthCheck(elasticSearchClient);
        environment.healthChecks().register("datacapture", dataCaptureHealthCheck);
        environment.jersey().register(dataCaptureHealthCheck);

        String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
        final HttpClient httpClient = new HttpClientBuilder(environment).using(configuration.getHttpClientConfiguration()).build(indexName);
        final ChannelsResource channelsResource = new ChannelsResource(httpClient);
        environment.jersey().register(channelsResource);

        final PlayResource playResource = new PlayResource(elasticSearchClient);
        environment.jersey().register(playResource);

        final ArtistResource artistResource = new ArtistResource(elasticSearchClient);
        environment.jersey().register(artistResource);

        String[] channels = configuration.getChannels(); // TODO pull from ChannelResource
        for (int i = 0; i < channels.length; i++) {
            String channel = channels[i];
            ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(channel);
            ScheduledExecutorService ses = sesBuilder.build();
            Runnable alarmTask = new DataCaptureJob(channel, httpClient, elasticSearchClient);
            int attemptFrequencySeconds = Integer.parseInt(elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.ATTEMPT_FREQ_NAME.value));
            ses.scheduleWithFixedDelay(alarmTask, 0, attemptFrequencySeconds, TimeUnit.SECONDS);
        }
    }

    private void getOrCreateIndex(Client elasticSearchClient) {
        String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
        IndicesExistsResponse indicesExistsResponse = elasticSearchClient.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        if (!indicesExistsResponse.isExists()) {
            final XContentBuilder mappingBuilder;
            try {
                String documentType = HelloWorldConfiguration.Constants.ES_TYPE.value;
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
