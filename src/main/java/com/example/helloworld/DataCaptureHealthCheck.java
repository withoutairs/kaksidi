package com.example.helloworld;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.health.HealthCheck;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.LoggerFactory;

public class DataCaptureHealthCheck extends HealthCheck {
    Client elasticSearchClient;
    final Logger logger = (Logger) LoggerFactory.getLogger(DataCaptureHealthCheck.class);

    public DataCaptureHealthCheck(Client elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }

    @Override
    protected Result check() throws Exception {
        String indexName = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.INDEX_NAME_NAME.value);
        String sampleFrequency = elasticSearchClient.settings().get(HelloWorldConfiguration.Constants.SAMPLE_FREQ_NAME.value);
        String lookBack = Integer.toString(Integer.parseInt(sampleFrequency) * 3);
        FilteredQueryBuilder queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                FilterBuilders.rangeFilter("_timestamp").from("now-" + lookBack + "s")
                        .to("now")
                        .includeLower(false)
                        .includeUpper(true));
        logger.debug(queryBuilder.buildAsBytes().toUtf8());
        SearchResponse searchResponse = elasticSearchClient.prepareSearch(indexName).
                setTypes(HelloWorldConfiguration.Constants.ES_TYPE.value).
                setQuery(queryBuilder)
                .get();
        long totalHits = searchResponse.getHits().getTotalHits();
        String blurb = String.format("Sampling every %s seconds, found %s in the last %s seconds.", sampleFrequency, totalHits, lookBack);
        if (totalHits > 0) {
            return Result.healthy(blurb);
        } else {
            return Result.unhealthy(blurb);
        }
    }
}
