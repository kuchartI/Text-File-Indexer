package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;

import java.nio.file.Path;
import java.util.Set;

public abstract class Index {
     abstract void indexFile(Path path, Token token);

     abstract void reIndexFile(Path path, Token token);

     abstract Set<Path> searchFiles(String queryWord);

     abstract void removeFileFromIndex(Path path);

     abstract void cleanupIndex();
}
