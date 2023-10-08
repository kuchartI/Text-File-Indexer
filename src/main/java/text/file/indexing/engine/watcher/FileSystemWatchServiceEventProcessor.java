package text.file.indexing.engine.watcher;

import text.file.indexing.engine.core.index.TextFileIndexer;

import java.nio.file.*;
import java.util.Map;
import java.util.logging.Logger;

public class FileSystemWatchServiceEventProcessor {

    private static final Logger LOGGER = Logger.getLogger(FileSystemWatchServiceEventProcessor.class.getName());

    private final TextFileIndexer textFileIndexer;

    private final Map<WatchKey, Path> fileNameDirPath;

    private final FileSystemWatchServiceWatcher fileSystemWatchServiceWatcher;


    public FileSystemWatchServiceEventProcessor(TextFileIndexer textFileIndexer, Map<WatchKey, Path> fileNameDirPath,
                                                FileSystemWatchServiceWatcher fileSystemWatchServiceWatcher) {
        this.textFileIndexer = textFileIndexer;
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
            textFileIndexer.removeFromIndex(fullPath);
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
            textFileIndexer.indexFile(fullPath);
            LOGGER.info("Create new dir/file " + fileName);
        }
    }

    private void executeFileModifyEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            if (Files.exists(fullPath)) {
                textFileIndexer.reIndexFile(fullPath);
                LOGGER.info("File modified: " + fullPath);
            }
        }
    }
}
