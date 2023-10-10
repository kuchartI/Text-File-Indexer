package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * The class accepts as input an implementation of {@link Index},
 * which is responsible for indexing the text file and {@link Token}.
 */
public class SimpleTextFileIndexer implements TextFileIndexer {
    private final Index index;
    private final Token token;

    public SimpleTextFileIndexer() {
        this.token = Token.defaultWhiteSpaceToken();
        this.index = new InvertedIndex();
    }

    public SimpleTextFileIndexer(Token token) {
        this.token = token;
        this.index = new InvertedIndex();
    }

    public SimpleTextFileIndexer(Token token, Index index) {
        this.token = token;
        this.index = index;
    }

    public void indexFiles(Collection<Path> paths) {
        index.addFilesToIndex(paths, token);
        index.indexFiles(token);
    }

    public void indexFile(Path path) {
        index.addFilesToIndex(Set.of(path), token);
        index.indexFiles(token);
    }

    public void removeFromIndex(Path path) {
        index.removeFileFromIndex(path);
        index.cleanupIndex();
    }

    public void reIndexFile(Path path) {
        index.reIndexFile(path, token);
    }

    @Override
    public Set<Path> searchFiles(String queryWord) {
        if (queryWord == null || queryWord.trim().isEmpty()) {
            throw new IllegalArgumentException("queryWord must not be null or blank");
        }
        return index.searchFiles(queryWord);
    }
}