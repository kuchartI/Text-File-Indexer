package text.file.indexing.engine.watcher;

import text.file.indexing.engine.core.index.TextFileIndexer;
import text.file.indexing.engine.utils.PathValidator;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FileSystemWatcher {

    private static final Logger LOGGER = Logger.getLogger(FileSystemWatcher.class.getName());

    private final Map<WatchKey, Path> fileNameDirPath;

    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private WatchService watchService;

    private final FileSystemEventProcessor fileSystemEventProcessor;

    private volatile boolean stopFlag = true;

    public FileSystemWatcher(TextFileIndexer textFileIndexer) {
        fileNameDirPath = new ConcurrentHashMap<>();
        fileSystemEventProcessor = new FileSystemEventProcessor(textFileIndexer, fileNameDirPath, this);
    }

    public void registerWatching(List<Path> paths) {
        Set<Path> pathList = PathValidator.getValidPathList(paths);
        EXECUTOR.execute(() -> initializeWatch(pathList));
    }

    public void stopWatching() {
        stopFlag = false;
    }

    private void initializeWatch(Set<Path> pathList) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerPaths(pathList);
            registerWatching(watchService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerPaths(Set<Path> pathList) {
        pathList.stream()
                .map(Path::getParent)
                .distinct()
                .forEach(this::registerPath);
    }

    void registerPath(Path path) {
        try {
            WatchKey key = path.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.OVERFLOW);
            fileNameDirPath.put(key, path);
        } catch (IOException e) {
            LOGGER.warning("can't register file");
        }
    }

    private void registerWatching(WatchService watchService) {
        while (stopFlag) {
            fileSystemEventProcessor.processEvent(getKey(watchService));
        }
        Thread.currentThread().interrupt();
        LOGGER.warning("Watch service interrupted");
    }

    private WatchKey getKey(WatchService watchService) {
        try {
            return watchService.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Watch service interrupted");
            return null;
        }
    }
}