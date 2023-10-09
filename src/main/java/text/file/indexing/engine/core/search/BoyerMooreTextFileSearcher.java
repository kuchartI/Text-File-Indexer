package text.file.indexing.engine.core.search;

import text.file.indexing.engine.core.index.TextFileIndexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class BoyerMooreTextFileSearcher implements TextFileSearcher {

    private static final Logger LOGGER = Logger.getLogger(BoyerMooreTextFileSearcher.class.getName());

    private final TextFileIndexer textFileIndexer;

    public BoyerMooreTextFileSearcher(TextFileIndexer textFileIndexer) {
        this.textFileIndexer = textFileIndexer;
    }

    public List<PathWithPosition> searchPathWithPosition(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern must not be null or blank");
        }
        Set<Path> wordFindInPaths = textFileIndexer.searchFiles(pattern);
        return wordFindInPaths.stream()
                .parallel()
                .map(it -> new PathWithPosition(it, boyerMooreSearcher(it, pattern)))
                .toList();
    }

    private List<Position> boyerMooreSearcher(Path filePath, String pattern) {
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            return boyerMooreSearcher(pattern, br);
        } catch (IOException e) {
            LOGGER.log(SEVERE, "A problem has occurred while searching word", e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for a pattern in a given file and returns a list of positions where the pattern is found.
     *
     * @param pattern the pattern to search for
     * @return a list of {@link Position} objects representing the positions where the pattern is found
     */
    private List<Position> boyerMooreSearcher(String pattern, BufferedReader br) throws IOException {
        List<Position> listOfPair = new ArrayList<>();
        Map<Character, Integer> chartTable = buildCharTable(pattern);
        int lineCounter = 0;
        String line;
        int patternSize = pattern.length();

        while ((line = br.readLine()) != null) {
            int lineSize = line.length();
            int currentIndex = 0;
            while (currentIndex <= (lineSize - patternSize)) {
                int currentMatch = patternSize - 1;
                while (currentMatch >= 0 &&
                        pattern.charAt(currentMatch) == line.charAt(currentIndex + currentMatch))
                    currentMatch--;

                if (currentMatch < 0) {
                    listOfPair.add(new Position(lineCounter, currentIndex));
                    if (currentIndex + patternSize < lineSize) {
                        char currentChar = line.charAt(currentIndex + patternSize);
                        currentIndex += patternSize - chartTable.getOrDefault(currentChar, -1);
                    } else {
                        currentIndex++;
                    }
                } else {
                    char currentChar = line.charAt(currentIndex + currentMatch);
                    int shiftChar = chartTable.getOrDefault(currentChar, patternSize);
                    currentIndex += Math.max(1, shiftChar);
                }
            }
            lineCounter++;

        }
        return listOfPair;
    }

    /**
     * Builds a character table mapping each character in the pattern to its corresponding shift value.
     *
     * @param pattern the pattern string
     * @return a map containing the characters in the pattern as keys and their corresponding shift values as values
     */
    Map<Character, Integer> buildCharTable(String pattern) {
        int patternLength = pattern.length();
        Map<Character, Integer> chartTable = new HashMap<>();

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i);
            if (i == patternLength - 1) {
                chartTable.put(c, patternLength - 1);
            } else {
                chartTable.put(c, patternLength - i - 2);
            }
        }

        return chartTable;
    }
}
