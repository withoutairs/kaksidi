package com.ktcb.kaksidi.datacapture;

import com.google.common.io.Files;
import com.ktcb.kaksidi.ChannelMetadataResponseFactory;
import com.ktcb.kaksidi.core.ChannelMetadataResponse;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Save the incoming String, which must parse to a channelMetadataResponse, to an appropriate ZIP.
 * <p>
 * We are not passing in a channelMetadataResponse here because the point is to capture the straight-from-SXM data,
 * and the channelMetadataResponse may vary over time.
 */
public class ZipStorageStrategy implements StorageStrategy {
    final Logger logger = (Logger) LoggerFactory.getLogger(ZipStorageStrategy.class);
    final String storagePath;

    public ZipStorageStrategy(String storagePath) {
        this.storagePath = storagePath;
    }

    public void apply(String responseBody) {
        try {
            ChannelMetadataResponse channelMetadataResponse = new ChannelMetadataResponseFactory().build(new JSONObject(responseBody));

            final String channelKey = channelMetadataResponse.getChannelKey();
            String filename = channelKey + "-" + DateTime.now().toString();

            final String zipName = storagePath + channelKey + "_" + DateTime.now().toString("yyyy-MM-dd") + ".zip";
            final File zipFile = new File(zipName);
            if (zipFile.exists()) {
                File bodyFile = File.createTempFile("temp","json");
                Files.write(responseBody.getBytes(), bodyFile);
                final File dest = new File(filename);
                bodyFile.renameTo(dest);
                addFilesToExistingZip(zipFile, new File[]{dest});
                dest.delete();
            } else {
                final FileOutputStream fileOutputStream = new FileOutputStream(zipName);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                ZipEntry zipEntry = new ZipEntry(filename);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(responseBody.getBytes());
                zipOutputStream.closeEntry();
                zipOutputStream.flush();
                zipOutputStream.close();
                fileOutputStream.close();
            }
            logger.info("Added " + filename + " to " + zipName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // http://stackoverflow.com/questions/3048669/how-can-i-add-entries-to-an-existing-zip-file-in-java
    //
    // wow.
    public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk) {
            throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (File f : files) {
                if (f.getName().equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles) {
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams
        zin.close();
        // Compress the files
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(files[i].getName()));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
        // Complete the ZIP file
        out.close();
        tempFile.delete();
    }
}
