package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;
import text.file.indexing.engine.watcher.FileWatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;

/**
 * InvertedIndex class represents an inverted index data structure used for text indexing and searching.
 */
public class InvertedIndex extends Index {

    private static final Logger LOGGER = Logger.getLogger(FileWatcher.class.getName());

    private final Map<String, Set<Path>> wordToFilesMap;

    public InvertedIndex() {
        wordToFilesMap = new ConcurrentHashMap<>();
    }

    void reIndexFile(Path path, Token token) {
        removeFileFromIndex(path);
        indexFile(path, token);
        cleanupIndex();
    }

    void indexFile(Path path, Token token) {
        try {
            var lines = readLines(path);
            var words = tokenize(lines, token);
            indexWords(words, path);
        } catch (IOException e) {
            LOGGER.log(SEVERE, "A problem has occurred while indexing the file.", e);
            throw new RuntimeException();
        }
    }

    private List<String> readLines(Path path) throws IOException {
        try (Stream<String> stream = Files.lines(path)) {
            return stream.toList();
        }
    }

    private List<String> tokenize(List<String> lines, Token token) {
        return lines.stream()
                .flatMap(line -> Arrays.stream(line.split(token.token())))
                .map(String::toLowerCase)
                .toList();
    }

    private void indexWords(List<String> words, Path path) {
        words.forEach(word ->
                wordToFilesMap.compute(word, (k, v) -> {
                    Set<Path> paths = v == null ? new HashSet<>() : v;
                    paths.add(path);
                    return paths;
                }));
    }

    protected Set<Path> searchFiles(String queryWord) {
        queryWord = queryWord.toLowerCase();
        return wordToFilesMap.getOrDefault(queryWord, Collections.emptySet());
    }

    protected void removeFileFromIndex(Path path) {
        wordToFilesMap.values().forEach(files -> files.remove(path));
    }

    protected void cleanupIndex() {
        wordToFilesMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}