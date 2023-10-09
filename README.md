# Text File Indexer
**CI for 'master' branch**  
[![CI master](https://github.com/kuchartI/Text-File-Indexer/actions/workflows/maven-ci.yml/badge.svg?branch=master)](https://github.com/kuchartI/Text-File-Indexer/actions/workflows/maven-ci.yml)

library that:

- Indexes specified files and directories.
- Queries files containing a given word.
- Reacts to changes in the (watched part of) filesystem.
- Supports custom tokens for indexing files.

## Getting Started

### Prerequisites

- Java language version >= 17
- Maven version >= 3

### Creating the indexer

There are two version of TextFileIndexer

- [SimpleTextFileIndexer](src/main/java/text/file/indexing/engine/core/index/SimpleTextFileIndexer.java) is used to
  index files and directories once.
- [WatcherTextFileIndexer](src/main/java/text/file/indexing/engine/core/index/WatcherTextFileIndexer.java) additionally
  reacts to changes in the watch part of the file system.
  Automatically reindex modified files, index new files and remove from index deleted files.

Both classes require [Token](src/main/java/text/file/indexing/engine/core/Token.java)
and [Index](src/main/java/text/file/indexing/engine/core/index/Index.java) implementation.
WatcherTextFileIndexer additionally
requires [FileSystemWatcher](src/main/java/text/file/indexing/engine/watcher/FileSystemWatcher.java) implementation.
However, you can use default constructors which provide default implementations
(Token : " ",
Index : [InvertedIndex](src/main/java/text/file/indexing/engine/core/index/InvertedIndex.java),
FileSystemWatcher : [FileSystemWatchServiceWatcher](src/main/java/text/file/indexing/engine/watcher/FileSystemWatchServiceWatcher.java)

### Usage

Example of using SimpleFileTextIndexer:

```
List<Path> pathList = new ArrayList<>(...);
SimpleFileTextIndexer indexer = new SimpleFileTextIndexer();
indexer.indexFiles(pathList);

String searchWord = "someString";
Set<Path> searchWordInFiles = indexer.searchWord(searchWord);
```

Example of using WatcherTextFileIndexer:

```
List<Path> pathList = new ArrayList<>(...);
WatcherTextFileIndexer indexer = new WatcherTextFileIndexer();
indexer.startWathing(pathList);

String searchWord = "someString";
Set<Path> searchWordInFiles = indexer.searchWord(searchWord);

...
indexer.stopWatching();
```

To get the position of search pattern in file, use [TextFileSearcher](src/main/java/text/file/indexing/engine/core/search/TextFileSearcher.java)'s `searchPathAndPosition`.
The library provides the [BoyerMooreTextFileSearcher](src/main/java/text/file/indexing/engine/core/search/BoyerMooreTextFileSearcher.java), the implementation of Boyer-Moore algorithm.

Example of using BoyerMooreTextFileSearcher:
```
TextFileIndexer indexer = ...
TextFileSearcher searcher = new BoyerMooreTextFileSearcher(indexer);
List<PathWithPosition> pathWithPositions = searcher.searchPathWithPosition("somePattern");
```