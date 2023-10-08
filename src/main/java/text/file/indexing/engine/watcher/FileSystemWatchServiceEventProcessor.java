package text.file.indexing.engine.watcher;

import text.file.indexing.engine.core.index.SimpleTextFileIndexer;

import java.nio.file.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The FileSystemWatchServiceEventProcessor class is responsible for processing events triggered by
 * the file system watch service. It listens for file system events such as file modifications, creations,
 * and deletions, and performs corresponding actions on the indexed files and directories.
 */
public class FileSystemWatchServiceEventProcessor {

    private static final Logger LOGGER = Logger.getLogger(FileSystemWatchServiceEventProcessor.class.getName());

    private final SimpleTextFileIndexer simpleTextFileIndexer;

    private final Map<WatchKey, Path> fileNameDirPath;

    private final FileSystemWatchServiceWatcher fileSystemWatchServiceWatcher;


    public FileSystemWatchServiceEventProcessor(SimpleTextFileIndexer simpleTextFileIndexer, Map<WatchKey, Path> fileNameDirPath,
                                                FileSystemWatchServiceWatcher fileSystemWatchServiceWatcher) {
        this.simpleTextFileIndexer = simpleTextFileIndexer;
        this.fileNameDirPath = fileNameDirPath;
        this.fileSystemWatchServiceWatcher = fileSystemWatchServiceWatcher;
    }

    void processEvent(WatchKey key) {
        if (key == null)
            return;

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            Path fileName = (Path) event.context();
            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                executeFileModifyEvent(key, fileName);
            } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                executeFileCreateEvent(key, fileName);
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                executeFileDeleteEvent(key, fileName);
            } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                LOGGER.warning("Too many changes at once, some changes may be lost.");
            }
        }
        key.reset();
    }

    private void executeFileDeleteEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            removeDirFromWatching(fullPath);
            simpleTextFileIndexer.removeFromIndex(fullPath);
            LOGGER.warning("File deleting " + fullPath);
        }
    }

    private void removeDirFromWatching(Path fullPath) {
        fileNameDirPath.forEach((innerKey, value) -> {
            if (value.equals(fullPath)) {
                innerKey.cancel();
                fileNameDirPath.remove(innerKey);
                LOGGER.warning("Directory remove from watching " + fullPath);
            }
        });
    }

    private void executeFileCreateEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            if (Files.isDirectory(fullPath)) {
                fileSystemWatchServiceWatcher.registerPath(fullPath);
            }
            simpleTextFileIndexer.indexFile(fullPath);
            LOGGER.info("Create new dir/file " + fileName);
        }
    }

    private void executeFileModifyEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            if (Files.exists(fullPath)) {
                simpleTextFileIndexer.reIndexFile(fullPath);
            }
        }
    }
}
