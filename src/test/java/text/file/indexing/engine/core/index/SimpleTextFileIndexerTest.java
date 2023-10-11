package text.file.indexing.engine.core.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import text.file.indexing.engine.core.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static text.file.indexing.engine.Fixtures.*;

public class SimpleTextFileIndexerTest {

    private SimpleTextFileIndexer simpleTextFileIndexer;
    private InvertedIndex invertedIndex;


    @BeforeEach
    void setUp() {
        invertedIndex = new InvertedIndex();
        simpleTextFileIndexer = new SimpleTextFileIndexer(Token.defaultWhiteSpaceToken(), invertedIndex);
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
    void testConcurrentIndexAndRemoveFromIndex() throws IOException, InterruptedException {
        Path tempDir1 = createTempDirWithFiles(113);
        Path tempDir2 = createTempDirWithFiles(100);
        Path tempFile1 = createTempFileWithContent("file1.txt", "test 1");
        invertedIndex.addFilesToIndex(List.of(tempDir1, tempDir2, tempFile1), Token.defaultWhiteSpaceToken());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> invertedIndex.indexFiles(Token.defaultWhiteSpaceToken()));

        executor.execute(() -> simpleTextFileIndexer.removeFromIndex(tempDir1));

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(101, simpleTextFileIndexer.searchFiles("test").size());

        deleteDir(tempDir1);
        deleteDir(tempDir2);
        Files.delete(tempFile1);

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

}
