package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HelloWorldConfiguration extends Configuration {
    public enum Constants {
        INDEX_NAME_NAME("com.example.helloworld.indexName");
        public final String value;

        Constants(String value) {
            this.value = value;
        }
    }

    ;
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

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
