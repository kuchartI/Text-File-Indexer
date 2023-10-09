package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;

import java.nio.file.Path;
import java.util.Set;

/**
 * An abstract class representing an Index for searching files.
 * You can write your own index, inherit it from this abstract class and use it to index text files.
 */
public abstract class Index {
    abstract void indexFile(Path path, Token token);

    abstract void reIndexFile(Path path, Token token);

    abstract Set<Path> searchFiles(String queryWord);

    abstract void removeFileFromIndex(Path path);

    abstract void cleanupIndex();
}
