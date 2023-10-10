package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;
import text.file.indexing.engine.utils.PathValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * InvertedIndex class represents an inverted index data structure used for text indexing and searching.
 */
class InvertedIndex extends Index {

    private static final Logger LOGGER = Logger.getLogger(InvertedIndex.class.getName());

    private final Map<String, Set<Path>> wordToFilesMap;

    private final Map<Path, AtomicBoolean> processedPaths;

    public InvertedIndex() {
        wordToFilesMap = new ConcurrentHashMap<>();
        processedPaths = new ConcurrentHashMap<>();
    }

    void reIndexFile(Path path, Token token) {
        removeFileFromIndex(path);
        addFilesToIndex(Set.of(path), token);
        indexFiles(token);
        cleanupIndex();
    }

    void addFilesToIndex(Collection<Path> paths, Token token) {
        Set<Path> validPaths = PathValidator.getValidPathSet(paths);
        validPaths.forEach(path -> processedPaths.put(path, new AtomicBoolean(true)));
    }

    void indexFiles(Token token) {
        processedPaths.forEach((key, value) -> indexFile(key, token));
    }

    void indexFile(Path path, Token token) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isProcessed = processedPaths.getOrDefault(path, new AtomicBoolean(false)).get();
            while ((line = reader.readLine()) != null && isProcessed) {
                isProcessed = processedPaths.getOrDefault(path, new AtomicBoolean(false)).get();
                if (isProcessed) {
                    var words = tokenize(line, token);
                    indexWords(words, path);
                }
            }
        } catch (IOException e) {
            LOGGER.log(SEVERE, "A problem has occurred while indexing the file.", e);
        }
    }

    private List<String> tokenize(String line, Token token) {
        return Arrays.stream(line.split(token.token()))
                .map(String::toLowerCase)
                .toList();
    }

    private void indexWords(List<String> words, Path path) {
        words.forEach(word ->
                wordToFilesMap.compute(word, (k, v) -> {
                    Set<Path> paths = v == null ? new ConcurrentSkipListSet<>() : v;
                    paths.add(path);
                    return paths;
                }));
    }

    Set<Path> searchFiles(String queryWord) {
        queryWord = queryWord.toLowerCase();
        return wordToFilesMap.getOrDefault(queryWord, Collections.emptySet());
    }


    void removeFileFromIndex(Path path) {
        processedPaths.forEach((key, atomicBoolean) -> {
            if (key.startsWith(path)) {
                atomicBoolean.compareAndSet(true, false);
            }
        });
        wordToFilesMap.values()
                .forEach(files -> files.removeIf(cur -> cur.startsWith(path)));
    }

    void cleanupIndex() {
        processedPaths.entrySet().removeIf(entry -> !entry.getValue().get());
        wordToFilesMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}