package text.file.indexing.engine.core.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import text.file.indexing.engine.core.index.SimpleTextFileIndexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static text.file.indexing.engine.Fixtures.createTempFileWithContent;

public class BoyerMooreTextFileSearcherTest {

    private BoyerMooreTextFileSearcher searcher;

    private SimpleTextFileIndexer simpleTextFileIndexer;

    @BeforeEach
    void setUp() {
        simpleTextFileIndexer = new SimpleTextFileIndexer();
        searcher = new BoyerMooreTextFileSearcher(simpleTextFileIndexer);
    }

    @Test
    void testSearchPathWithPosition() throws IOException {
        Path tempFile = createTempFileWithContent("test.txt",
                """
                        This is a test fiile.
                        This is a test fille.
                        This is a test fiqle.
                        This is a test file.
                        file is a test This.
                        """);

        Path tempFile2 = createTempFileWithContent("test2.txt",
                """
                        This is a test fiole.
                        This is a test fiole.
                        This is a test fiole.
                        This is a test fiole.
                        file is a file file.
                        """);

        Path tempFile3 = createTempFileWithContent("test2.txt", "emptyfile");

        simpleTextFileIndexer.indexFiles(List.of(tempFile, tempFile2, tempFile3));

        List<PathWithPosition> pathWithPositions = searcher.searchPathWithPosition("file");

        List<PathWithPosition> expected = new ArrayList<>();
        expected.add(new PathWithPosition(tempFile, List.of(new Position(3, 15),
                new Position(4, 0))));
        expected.add(new PathWithPosition(tempFile2, List.of(new Position(4, 0),
                new Position(4, 10),
                new Position(4, 15))));

        assertEquals(expected, pathWithPositions);
    }

    @Test
    void testBuildCharTable() {
        String pattern = "abc";
        BoyerMooreTextFileSearcher searcher = new BoyerMooreTextFileSearcher(null);
        Map<Character, Integer> charTable = searcher.buildCharTable(pattern);

        assertEquals(1, charTable.get('a'));
        assertEquals(0, charTable.get('b'));
        assertEquals(2, charTable.get('c'));
    }
}
