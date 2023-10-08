package text.file.indexing.engine.watcher;

import java.nio.file.Path;
import java.util.Collection;

public interface FileSystemWatcher {

    void startWatching(Collection<Path> paths);

    void stopWatching();
}
