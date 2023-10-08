package text.file.indexing.engine.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for validating and retrieving valid paths.
 */
public class PathValidator {

    private static final Logger LOGGER = Logger.getLogger(PathValidator.class.getName());

    public static Set<Path> getValidPathList(Collection<Path> paths) {
        if (paths == null) {
            throw new IllegalArgumentException("paths must not be null");
        }
        return paths.stream()
                .flatMap(list -> getValidPathList(list).stream())
                .collect(Collectors.toSet());
    }

    public static Set<Path> getValidPathList(Path path) {
        validatePath(path);
        if (Files.notExists(path)) {
            return Collections.emptySet();
        } else if (Files.isDirectory(path)) {
            return findAllFilesInDir(path);
        } else {
            return Set.of(path);
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

    private static Set<Path> findAllFilesInDir(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(Files::isRegularFile).collect(Collectors.toSet());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return Collections.emptySet();
    }
}
