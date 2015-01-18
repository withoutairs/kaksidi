package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HelloWorldConfiguration extends Configuration {
    public enum Constants {
        INDEX_NAME_NAME("com.example.helloworld.indexName"),
        ES_TYPE("channelMetadataResponse"),
        ATTEMPT_FREQ_NAME("com.example.helloworld.attemptFrequencySeconds"),
        SAMPLE_FREQ_NAME("com.example.helloworld.sampleFrequencySeconds");

        public final String value;

        Constants(String value) {
            this.value = value;
        }
    }

    ;
    @JsonProperty
    public String[] getChannels() {
        return channels;
    }

    @JsonProperty
    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    @NotNull
    private String[] channels;

    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @Valid
    @NotNull
    private ElasticSearchClientFactory elasticSearchClient = new ElasticSearchClientFactory();

    @JsonProperty("elasticSearch")
    public ElasticSearchClientFactory getElasticSearchClientFactory() {
        return elasticSearchClient;
    }


    @JsonProperty("elasticSearch")
    public void setElasticSearchClientFactory(ElasticSearchClientFactory elasticSearchClientFactory) {
        this.elasticSearchClient = elasticSearchClientFactory;
    }
}
