package text.file.indexing.engine.core.index;

import text.file.indexing.engine.core.Token;
import text.file.indexing.engine.watcher.FileSystemWatchServiceWatcher;
import text.file.indexing.engine.watcher.FileSystemWatcher;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * Implementation of the TextFileIndexer interface that integrates with a file system watcher.
 * This class indexes text files using SimpleTextFileIndexer and utilizes a provided FileSystemWatcher for monitoring file changes.
 */
public class WatcherTextFileIndexer implements TextFileIndexer {

    private final SimpleTextFileIndexer simpleTextFileIndexer;
    private final FileSystemWatcher watcher;


    public WatcherTextFileIndexer(Index index, Token token, FileSystemWatcher watcher) {
        this.simpleTextFileIndexer = new SimpleTextFileIndexer(token, index);
        this.watcher = watcher;
    }

    public WatcherTextFileIndexer() {
        Token token = Token.defaultWhiteSpaceToken();
        Index index = new InvertedIndex();
        simpleTextFileIndexer = new SimpleTextFileIndexer(token, index);
        this.watcher = new FileSystemWatchServiceWatcher(simpleTextFileIndexer);
    }

    public void startWatching(Collection<Path> paths) {
        simpleTextFileIndexer.indexFiles(paths);
        watcher.startWatching(paths);
    }

    public void stopWatching() {
        watcher.stopWatching();
    }

    @Override
    public Set<Path> searchFiles(String queryWord) {
        return simpleTextFileIndexer.searchFiles(queryWord);
    }
}
