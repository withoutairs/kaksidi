package com.ktcb.kaksidi.datacapture;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestZipStorageStrategy {
    @Test
    public void test1() {

        URL resourceUrl = getClass().getResource("/sample-channelMetadataResponse.json");
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(resourceUrl.toURI());
            File file = resourcePath.toFile();
            String body = FileUtils.readFileToString(file);
            ZipStorageStrategy strategy = new ZipStorageStrategy();
            strategy.apply(body);
            TestLogger logger = TestLoggerFactory.getTestLogger(ZipStorageStrategy.class);
            Assert.assertEquals(1, logger.getLoggingEvents().size());
            Assert.assertTrue(logger.getLoggingEvents().get(0).getMessage().startsWith("Added"));
        } catch (URISyntaxException e) {
            Assert.fail("Spacetime failure, hardcoded text failed");
        } catch (IOException e) {
            Assert.fail("Come on, couldn't read a file from disk");
        }

    }
}
