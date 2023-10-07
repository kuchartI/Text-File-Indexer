package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;
import text.file.indexing.engine.util.PathValidator;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class TextFileIndexer {
    private final Index index;
    private final Token token;

    public TextFileIndexer(Token token, Index index) {
        this.token = token;
        this.index = index;
    }

    public void indexFiles(List<Path> paths) {
        PathValidator.getValidPathList(paths)
                .forEach(this::indexFile);
    }

    public void indexFile(Path path) {
        PathValidator.getValidPathList(path)
                .forEach(it -> index.indexFile(it, token));
    }

    public void removeFromIndex(Path path) {
        index.removeFileFromIndex(path);
        index.cleanupIndex();
    }

    public void reIndexFile(Path path) {
        PathValidator.getValidPathList(path)
                .forEach(it -> index.reIndexFile(it, token));
    }

    public Set<Path> searchFiles(String queryWord) {
        if (queryWord == null || queryWord.trim().isEmpty()) {
            throw new IllegalArgumentException("queryWord must not be null or blank");
        }
        return index.searchFiles(queryWord);
    }
}