package text.file.indexing.engine.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PathValidatorTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test");
    }

    @Test
    void testGetValidPathSet() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(tempDir, "testFile", ".txt");
            Collection<Path> paths = Set.of(tempFile);

            Set<Path> validPaths = PathValidator.getValidPathSet(paths);

            assertEquals(1, validPaths.size());
            assertTrue(validPaths.contains(tempFile));
        } catch (IOException e) {
            fail("Failed to create temporary file: " + e.getMessage());
        } finally {

            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    void testGetValidPathSetWithNullPaths() {
        assertThrows(IllegalArgumentException.class, () -> PathValidator.getValidPathSet(null));
    }


    @Test
    void testGetValidPathSetWithEmptyPath() {
        Collection<Path> emptyPaths = Set.of();

        Set<Path> validPaths = PathValidator.getValidPathSet(emptyPaths);

        assertTrue(validPaths.isEmpty());
    }

    @Test
    void testGetValidPathSetWithDirectory() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path  tempFile = Files.createTempFile(subDir, "testFile", ".txt");
        Collection<Path> paths = Set.of(subDir);

        Set<Path> validPaths = PathValidator.getValidPathSet(paths);

        assertEquals(1, validPaths.size());
        assertTrue(validPaths.contains(tempFile));
    }

}
