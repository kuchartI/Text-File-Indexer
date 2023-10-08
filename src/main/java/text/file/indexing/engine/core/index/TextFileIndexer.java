package text.file.indexing.engine.core.index;

import java.nio.file.Path;
import java.util.Set;

public interface TextFileIndexer {

    Set<Path> searchFiles(String queryWord);
}
