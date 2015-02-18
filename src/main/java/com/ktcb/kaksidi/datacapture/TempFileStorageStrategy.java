package com.ktcb.kaksidi.datacapture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempFileStorageStrategy implements StorageStrategy {
    final Logger logger = (Logger) LoggerFactory.getLogger(TempFileStorageStrategy.class);
    public void apply(String responseBody)  {
        try
        {
            final Path path = Files.createTempFile("kaksidi", ".json");
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
            bufferedWriter.write(responseBody);
            bufferedWriter.close();
            logger.info("Saved to " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
