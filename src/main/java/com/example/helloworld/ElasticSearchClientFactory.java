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

    public Client build(Environment environment) {
        Settings settings = ImmutableSettings.settingsBuilder().put("com.example.helloworld.indexName", this.getIndexName()).build();
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
