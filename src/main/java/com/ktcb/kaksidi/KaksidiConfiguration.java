package com.ktcb.kaksidi;

import com.bazaarvoice.dropwizard.assets.AssetsBundleConfiguration;
import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ktcb.kaksidi.datacapture.DataCaptureJobConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class KaksidiConfiguration extends Configuration implements AssetsBundleConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    private final AssetsConfiguration assets = new AssetsConfiguration();
    @NotNull
    private String[] channels;
    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();
    @Valid
    @NotNull
    private ElasticSearchClientFactory elasticSearchClient = new ElasticSearchClientFactory();
    @NotNull
    private DataCaptureJobConfiguration dataCaptureJobConfiguration = new DataCaptureJobConfiguration();

    @Override
    public AssetsConfiguration getAssetsConfiguration() {
        return assets;
    }

    @JsonProperty
    public String[] getChannels() {
        return channels;
    }

    @JsonProperty
    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @JsonProperty("elasticSearch")
    public ElasticSearchClientFactory getElasticSearchClientFactory() {
        return elasticSearchClient;
    }


    @JsonProperty("elasticSearch")
    public void setElasticSearchClientFactory(ElasticSearchClientFactory elasticSearchClientFactory) {
        this.elasticSearchClient = elasticSearchClientFactory;
    }

    @JsonProperty("dataCapture")
    public DataCaptureJobConfiguration getDataCaptureJobConfiguration() {
        return dataCaptureJobConfiguration;
    }

    @JsonProperty("dataCapture")
    public void setDataCaptureJobConfiguration(DataCaptureJobConfiguration dataCaptureJobConfiguration) {
        this.dataCaptureJobConfiguration = dataCaptureJobConfiguration;
    }

    public enum Constants {
        INDEX_NAME_NAME("com.ktcb.kaksidi.indexName"),
        ES_TYPE("channelMetadataResponse");

        public final String value;

        Constants(String value) {
            this.value = value;
        }
    }
}
