package text.file.indexing.engine.core.search;

import java.util.List;

public interface TextFileSearcher {
    List<PathWithPosition> searchPathWithPosition(String searchWord);
}
