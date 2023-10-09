package text.file.indexing.engine.core.search;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class PathWithPosition {
    private final Path path;
    private final List<Position> positions;


    PathWithPosition(Path path, List<Position> positions) {
        this.path = path;
        this.positions = positions;
    }

    public Path getPath() {
        return path;
    }

    public List<Position> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "PathWithPosition{" +
                "path=" + path +
                ", positions=" + positions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathWithPosition that)) return false;
        return Objects.equals(path, that.path) && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, positions);
    }
}
