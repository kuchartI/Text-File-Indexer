package text.file.indexing.engine;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class Fixtures {
    public static Path createTempFileWithContent(String fileName, String content) throws IOException {
        Path tempFile = Files.createTempFile(fileName, ".txt");
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }

    public static Path createTempDirWithFiles(int filesCount) throws IOException {
        Path tempDir = Files.createTempDirectory("myTempDir" + filesCount);
        for (int i = 1; i <= filesCount; i++) {
            String filename = "file" + i + ".txt";
            Path filePath = tempDir.resolve(filename);
            String content = "test " + i;
            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
        }
        return tempDir;
    }

    public static void deleteDir(Path path) throws IOException {

        if (Files.exists(path)) {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }
            });


        }
    }
}
