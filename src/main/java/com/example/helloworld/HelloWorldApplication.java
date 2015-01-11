package com.example.helloworld;

import ch.qos.logback.classic.Logger;
import com.example.helloworld.datacapture.DataCaptureJob;
import com.example.helloworld.health.TemplateHealthCheck;
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
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
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

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);


        final Client elasticSearchClient = configuration.getElasticSearchClientFactory().build(environment);

        addTimestampToIndex(elasticSearchClient);

        String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
        final HttpClient httpClient = new HttpClientBuilder(environment).using(configuration.getHttpClientConfiguration()).build(indexName);
        final ChannelsResource channelsResource = new ChannelsResource(httpClient);
        environment.jersey().register(channelsResource);

        final PlayResource playResource = new PlayResource(elasticSearchClient);
        environment.jersey().register(playResource);

        final ArtistResource artistResource = new ArtistResource(elasticSearchClient);
        environment.jersey().register(artistResource);

        // TODO dynamically, from a filtered list of channels (or as an interim read from config?)
        String channel = "leftofcenter";
        ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(channel);
        ScheduledExecutorService ses = sesBuilder.build();
        Runnable alarmTask = new DataCaptureJob(channel, httpClient, elasticSearchClient);
        ses.scheduleWithFixedDelay(alarmTask, 0, 1, TimeUnit.MINUTES); // TODO configure the interval
    }

    // really, one-time setup but no harm in repeating I suppose
    // TODO does this work?
    private void addTimestampToIndex(Client elasticSearchClient) {
        String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
        final CreateIndexRequestBuilder createIndexRequestBuilder = elasticSearchClient.admin().indices().prepareCreate(indexName);  // TODO this is getting crazy
        final XContentBuilder mappingBuilder;
        try {
            String documentType = HelloWorldConfiguration.Constants.ES_TYPE.value;
            mappingBuilder = jsonBuilder().startObject().
                    startObject(documentType) // this too
                        .startObject("_timestamp").field("enabled", "true").endObject() // TODO this will index timestamps based on now(); do I need to also force time nature on the appropriate fields from plays?
                    .endObject()
                    .endObject();
            createIndexRequestBuilder.addMapping(documentType, mappingBuilder);
        } catch (IOException e) {
            logger.error("Could not add timestamp to index", e);
        }
    }
}
