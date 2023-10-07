package text.file.indexing.engine.watcher;

import text.file.indexing.engine.core.index.TextFileIndexer;
import text.file.indexing.engine.util.PathValidator;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FileWatcher {

    private static final Logger LOGGER = Logger.getLogger(FileWatcher.class.getName());

    private final Map<WatchKey, Path> fileNameDirPath = new ConcurrentHashMap<>();

    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private WatchService watchService;

    private final TextFileIndexer textFileIndexer;

    private volatile boolean stopFlag = true;

    public FileWatcher(TextFileIndexer textFileIndexer) {
        this.textFileIndexer = textFileIndexer;
    }

    public void registerWatching(List<Path> paths) {
        List<Path> pathList = PathValidator.getValidPathList(paths);
        EXECUTOR.execute(() -> initializeWatch(pathList));
    }

    public void registerWatching(Path path) {
        List<Path> pathList = PathValidator.getValidPathList(path);
        EXECUTOR.execute(() -> initializeWatch(pathList));
    }

    public void stopWatching() {
        stopFlag = false;
    }

    private void initializeWatch(List<Path> pathList) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerPaths(pathList);
            registerWatching(watchService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerPaths(List<Path> pathList) {
        pathList.stream()
                .map(Path::getParent)
                .distinct()
                .forEach(this::registerPath);
    }

    private void registerPath(Path path) {
        try {
            WatchKey key = path.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.OVERFLOW);
            fileNameDirPath.put(key, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerWatching(WatchService watchService) {
        while (stopFlag) {
            processEvents(getKey(watchService));
        }
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

    private void processEvents(WatchKey key) {
        if (key == null)
            return;

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            Path fileName = (Path) event.context();
            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                executeFileModifyEvent(key, fileName);
            }  else if (kind == StandardWatchEventKinds.OVERFLOW) {
                LOGGER.warning("Too many changes at once, some changes may be lost.");
            }
        }
        key.reset();
    }

    private void executeFileDeleteEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            if (Files.isDirectory(fullPath)) {
                key.cancel();
                fileNameDirPath.remove(key);
                LOGGER.warning("File deleting " + fullPath);
            }
        }
    }

    private void executeFileCreateEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            registerPath(fullPath);
            textFileIndexer.indexFile(fullPath);
            LOGGER.info("Create new dir/file " + fileName);
        }
    }

    private void executeFileModifyEvent(WatchKey key, Path fileName) {
        if (fileNameDirPath.containsKey(key)) {
            Path fullPath = fileNameDirPath.get(key).resolve(fileName);
            if (Files.notExists(fullPath)) {
                executeFileDeleteEvent(key, fullPath);
                textFileIndexer.removeFromIndex(fullPath);
            } else {
                textFileIndexer.reIndexFile(fullPath);
                LOGGER.info("File modified: " + fullPath);
            }
        }
    }
}