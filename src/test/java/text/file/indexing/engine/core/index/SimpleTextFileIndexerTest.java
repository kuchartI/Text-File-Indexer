package text.file.indexing.engine.core.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleTextFileIndexerTest {

    private SimpleTextFileIndexer simpleTextFileIndexer;

    @BeforeEach
    void setUp() {
        simpleTextFileIndexer = new SimpleTextFileIndexer();
    }

    @Test
    void testIndexFileWithWhiteSpaceToken() throws IOException {
        Path tempFile = createTempFileWithContent("test.txt", "This is a test file.");
        Path tempFile2 = createTempFileWithContent("test2.txt", "This is a tes file.");
        simpleTextFileIndexer.indexFiles(List.of(tempFile, tempFile2));

        Set<Path> indexedFiles = simpleTextFileIndexer.searchFiles("test");
        assertEquals(1, indexedFiles.size());
        assertTrue(indexedFiles.contains(tempFile));
        Files.delete(tempFile);
    }

    @Test
    void testRemoveFileFromIndex() throws IOException {
        Path tempFile1 = createTempFileWithContent("file1.txt", "test 1");
        Path tempFile2 = createTempFileWithContent("file2.txt", "test 2");

        simpleTextFileIndexer.indexFile(tempFile1);
        simpleTextFileIndexer.indexFile(tempFile2);

        assertEquals(2, simpleTextFileIndexer.searchFiles("test").size());

        simpleTextFileIndexer.removeFromIndex(tempFile1);

        assertEquals(1, simpleTextFileIndexer.searchFiles("test").size());
        assertFalse(simpleTextFileIndexer.searchFiles("test").contains(tempFile1));
        Files.delete(tempFile1);
        Files.delete(tempFile2);
    }

    @Test
    void testCleanupIndex() throws IOException {
        Path tempFile1 = createTempFileWithContent("file1.txt", "test 1");
        Path tempFile2 = createTempFileWithContent("file2.txt", "test 2");

        simpleTextFileIndexer.indexFile(tempFile1);
        simpleTextFileIndexer.indexFile(tempFile2);

        assertEquals(2, simpleTextFileIndexer.searchFiles("test").size());

        simpleTextFileIndexer.removeFromIndex(tempFile1);
        simpleTextFileIndexer.removeFromIndex(tempFile2);

        assertEquals(0, simpleTextFileIndexer.searchFiles("test").size());
        Files.delete(tempFile1);
        Files.delete(tempFile2);
    }

    @Test
    void testSearchFiles_NotFound() {
        Set<Path> result = simpleTextFileIndexer.searchFiles("not_exist");
        assertEquals(Collections.emptySet(), result);
    }

    @Test
    void testSearchFiles_InputException() {
        assertThrows(IllegalArgumentException.class, () -> simpleTextFileIndexer.searchFiles("   "));
    }

    private Path createTempFileWithContent(String fileName, String content) throws IOException {
        Path tempFile = Files.createTempFile(fileName, ".txt");
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }
}
