package com.ktcb.kaksidi.datacapture;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.fest.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class TestZipStorageStrategy {
    @Test
    public void RawResponseSavesToZip() {
        final Logger logger = (Logger) LoggerFactory.getLogger(TestZipStorageStrategy.class);

        URL resourceUrl = getClass().getResource("/sample-channelMetadataResponse.json");
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(resourceUrl.toURI());
            File file = resourcePath.toFile();
            String body = FileUtils.readFileToString(file);
            File tempDirectoryForZip = Files.createTempDir();
            ZipStorageStrategy strategy = new ZipStorageStrategy(tempDirectoryForZip.getAbsolutePath() + File.separator);
            strategy.apply(body);
            final File[] files = tempDirectoryForZip.listFiles();
            if (Arrays.isNullOrEmpty(files)) {
                Assert.fail("No zips found in " + tempDirectoryForZip);
            }
            Assert.assertEquals(1, files.length);
            ZipFile zipFile = new ZipFile(files[0]);
            Assert.assertEquals(1, zipFile.size());
            zipFile.close();
        } catch (URISyntaxException e) {
            logger.error("test failed", e);
            Assert.fail("Spacetime failure, hardcoded text failed to parse");
        } catch (IOException e) {
            logger.error("test failed", e);
            Assert.fail("Come on, couldn't read a file from disk");
        }

    }
}
