package com.ktcb.kaksidi.datacapture;

import com.ktcb.kaksidi.ChannelMetadataResponseFactory;
import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Save the incoming String, which must parse to a channelMetadataResponse, to an appropriate ZIP.
 *
 * We are not passing in a channelMetadataResponse here because the point is to capture the straight-from-SXM data,
 * and the channelMetadataResponse may vary over time.
 */
public class ZipStorageStrategy implements StorageStrategy {
    final Logger logger = (Logger) LoggerFactory.getLogger(ZipStorageStrategy.class);
    final String storagePath;

    public ZipStorageStrategy(String storagePath) {
        this.storagePath = storagePath;
    }

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
            zipOutputStream.flush();
            zipOutputStream.close();
            fileOutputStream.close();
            logger.info("Added " + filename + " to " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
