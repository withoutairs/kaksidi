package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.hibernate.validator.constraints.NotEmpty;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class ElasticSearchClientFactory {
    @NotEmpty
    private String clusterName;

    @NotEmpty
    private String indexName;

    @NotEmpty
    private String attemptFrequencySeconds;

    @NotEmpty
    private String sampleFrequencySeconds;

    @JsonProperty
    public String getSampleFrequencySeconds() {
        return sampleFrequencySeconds;
    }

    @JsonProperty
    public void setSampleFrequencySeconds(String sampleFrequencySeconds) {
        this.sampleFrequencySeconds = sampleFrequencySeconds;
    }

    @JsonProperty
    String getClusterName() {
        return clusterName;
    }

    @JsonProperty
    void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @JsonProperty
    public String getIndexName() {
        return indexName;
    }

    @JsonProperty
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @JsonProperty
    public String getAttemptFrequencySeconds() {
        return attemptFrequencySeconds;
    }

    @JsonProperty
    public void setAttemptFrequencySeconds(String attemptFrequencySeconds) {
        this.attemptFrequencySeconds = attemptFrequencySeconds;
    }

    public Client build(Environment environment) {
        // TOOD yuck.  maybe better to extend Client and make a DataCaptureClient/Factory etc
        Settings settings = ImmutableSettings.settingsBuilder().
                put(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value, this.getIndexName()).
                put(HelloWorldConfiguration.Constants.ATTEMPT_FREQ_NAME.value, this.getAttemptFrequencySeconds()).
                put(HelloWorldConfiguration.Constants.SAMPLE_FREQ_NAME.value, this.getSampleFrequencySeconds()).
                build();
        Node node = nodeBuilder().clusterName(clusterName).settings(settings).node();
        final Client client = node.client();

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
                client.close();
            }
        });
        return client;
    }
}
