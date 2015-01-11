package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.hibernate.validator.constraints.NotEmpty;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class ElasticSearchClientFactory {
    @NotEmpty
    private String clusterName;

    @JsonProperty
    String getClusterName() {
        return clusterName;
    }

    @JsonProperty
    void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Client build(Environment environment) {
        Node node = nodeBuilder().clusterName(clusterName).node();
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
