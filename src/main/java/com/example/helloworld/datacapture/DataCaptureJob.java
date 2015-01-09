package com.example.helloworld.datacapture;

import ch.qos.logback.classic.Logger;
import org.apache.http.client.HttpClient;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataCaptureJob implements Runnable {
    public static final String SXM_TIMESTAMP_PATTERN = "MM-dd'-'kk:mm:'00'";
    private final String channel;
    private final HttpClient httpClient;
    private final String sxmTimestampUri = "https://www.siriusxm.com/metadata/pdt/en-us/json/channels/%s/timestamp/%s";

    public DataCaptureJob(String channel, HttpClient httpClient) {
        this.channel = channel;
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        final Logger logger = (Logger) LoggerFactory.getLogger(DataCaptureJob.class + channel);
        try {
            LocalDateTime nowInUtc = LocalDateTime.now(Clock.systemUTC());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(SXM_TIMESTAMP_PATTERN);
            String timestamp = nowInUtc.format(dateTimeFormatter);
            final String uri = String.format(sxmTimestampUri, channel, timestamp);
            // TODO actually do the get and save to ES
            logger.info("Capturing data for " + channel + " from " + uri);
        } catch (Exception e) {
            logger.error("Failed for channel=" + channel, e);
        }
    }
}
