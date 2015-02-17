import com.ktcb.kaksidi.datacapture.TempFileStorageStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestTempFileStorageStrategy {
    @Test
    public void test1() {

        URL resourceUrl = getClass().getResource("/sample-channelMetadataResponse.json");
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(resourceUrl.toURI());
            File file = resourcePath.toFile();
            String body = FileUtils.readFileToString(file);
            TempFileStorageStrategy strategy = new TempFileStorageStrategy();
            strategy.apply(body);
        } catch (URISyntaxException e) {
            Assert.fail("Spacetime failure, hardcoded text failed");
        } catch (IOException e) {
            Assert.fail("Come on, couldn't read a file from disk");
        }

    }
}
