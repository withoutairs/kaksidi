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

        URL resourceUrl = getClass().getResource("/sample-channelMetadataResponse-1.json");
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

    @Test
    public void MultipleSavesToZip() {
        final Logger logger = (Logger) LoggerFactory.getLogger(TestZipStorageStrategy.class);

        URL resourceUrl1 = getClass().getResource("/sample-channelMetadataResponse-1.json");
        URL resourceUrl2 = getClass().getResource("/sample-channelMetadataResponse-2.json");
        Path resourcePath = null;
        try {
            File tempDirectoryForZip = Files.createTempDir();
            ZipStorageStrategy strategy = new ZipStorageStrategy(tempDirectoryForZip.getAbsolutePath() + File.separator);

            resourcePath = Paths.get(resourceUrl1.toURI());
            File file = resourcePath.toFile();
            String body = FileUtils.readFileToString(file);
            strategy.apply(body);

            resourcePath = Paths.get(resourceUrl2.toURI());
            file = resourcePath.toFile();
            body = FileUtils.readFileToString(file);
            strategy.apply(body);

            // There should be a ZIP file
            final File[] files = tempDirectoryForZip.listFiles();
            if (Arrays.isNullOrEmpty(files)) {
                Assert.fail("No zips found in " + tempDirectoryForZip);
            }
            Assert.assertEquals(1, files.length);

            // It should have two files in it
            ZipFile zipFile = new ZipFile(files[0]);
            Assert.assertEquals(2, zipFile.size());

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
