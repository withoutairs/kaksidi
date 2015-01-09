package com.example.helloworld.datacapture;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class DataCaptureJob implements Runnable {
    private final String channel;

    public DataCaptureJob(String channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        final Logger logger = (Logger) LoggerFactory.getLogger(DataCaptureJob.class + channel);
        logger.info("Capturing data for " + channel);
    }
}
