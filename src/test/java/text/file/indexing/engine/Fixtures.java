package text.file.indexing.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Fixtures {
    public static Path createTempFileWithContent(String fileName, String content) throws IOException {
        Path tempFile = Files.createTempFile(fileName, ".txt");
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }
}
