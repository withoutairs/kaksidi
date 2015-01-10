package com.example.helloworld;

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
import org.elasticsearch.client.Client;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

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

        final HttpClient httpClient = new HttpClientBuilder(environment).using(configuration.getHttpClientConfiguration())
                .build("xm");
        final ChannelsResource channelsResource =
                new ChannelsResource(httpClient);
        environment.jersey().register(channelsResource);

        final Client elasticSearchClient = configuration.getElasticSearchClientFactory().build(environment);
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
}
