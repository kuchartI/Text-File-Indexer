package text.file.indexing.engine.watcher;

import text.file.indexing.engine.core.index.TextFileIndexer;
import text.file.indexing.engine.utils.PathValidator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FileSystemWatchServiceWatcher implements FileSystemWatcher {

    private static final Logger LOGGER = Logger.getLogger(FileSystemWatchServiceWatcher.class.getName());

    private final Map<WatchKey, Path> fileNameDirPath;

    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final WatchService watchService;

    private final FileSystemWatchServiceEventProcessor fileSystemWatchServiceEventProcessor;

    private volatile boolean runningFlag;

    public FileSystemWatchServiceWatcher(TextFileIndexer textFileIndexer) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        runningFlag = true;
        fileNameDirPath = new ConcurrentHashMap<>();
        fileSystemWatchServiceEventProcessor = new FileSystemWatchServiceEventProcessor(textFileIndexer, fileNameDirPath, this);
    }

    public void startWatching(Collection<Path> paths) {
        runningFlag = true;
        Set<Path> pathList = PathValidator.getValidPathList(paths);
        EXECUTOR.execute(() -> initializeWatch(pathList));
    }

    public void stopWatching() {
        runningFlag = false;
    }

    private void initializeWatch(Set<Path> pathList) {
        registerPaths(pathList);
        startWatching(watchService);
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

    private void startWatching(WatchService watchService) {
        while (runningFlag) {
            fileSystemWatchServiceEventProcessor.processEvent(getKey(watchService));
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