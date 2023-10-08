package text.file.indexing.engine.watcher;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Interface for a file system watcher.
 */
public interface FileSystemWatcher {

    void startWatching(Collection<Path> paths);

    void stopWatching();
}
