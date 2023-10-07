package text.file.indexing.engine.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility class for validating and retrieving valid paths.
 */
public class PathValidator {

    public static List<Path> getValidPathList(List<Path> paths) {
        if (paths == null) {
            throw new IllegalArgumentException("paths must not be null");
        }
        return paths.stream()
                .flatMap(list -> getValidPathList(list).stream())
                .toList();
    }

    public static List<Path> getValidPathList(Path path) {
        validatePath(path);
        if (Files.notExists(path)) {
            return Collections.emptyList();
        } else if (Files.isDirectory(path)) {
            return findAllFilesInDir(path);
        } else {
            return List.of(path);
        }
    }

    private static void validatePath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (Files.exists(path) && !Files.isDirectory(path) && !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("path must be file or directory");
        }
    }

    private static List<Path> findAllFilesInDir(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
