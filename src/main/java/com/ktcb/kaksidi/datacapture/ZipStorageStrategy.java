package com.ktcb.kaksidi.datacapture;

import ch.qos.logback.classic.Logger;
import com.ktcb.kaksidi.ChannelMetadataResponseFactory;
import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStorageStrategy implements StorageStrategy {
    final Logger logger = (Logger) LoggerFactory.getLogger(ZipStorageStrategy.class);
    final String storagePath = "/tmp/kaksidi/"; // TODO config obvs
    public void apply(String responseBody)  {
        try
        {
            ChannelMetadataResponse channelMetadataResponse = new ChannelMetadataResponseFactory().build(new JSONObject(responseBody));

            final String channelKey = channelMetadataResponse.getChannelKey();
            String filename = channelKey + "-" + DateTime.now().toString();

            final String name = storagePath + channelKey + ".zip";
            final FileOutputStream fileOutputStream = new FileOutputStream(name);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry zipEntry = new ZipEntry(filename);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(responseBody.getBytes());
            zipOutputStream.closeEntry();
            fileOutputStream.close();
            logger.info("Added " + filename + " to " + name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
