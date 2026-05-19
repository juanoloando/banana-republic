package model.board;

import model.resource.ResourceType;
import java.util.*;
import java.util.stream.Collectors;

public class Board {

    private final Map<HexCoordinate, HexTile> tiles;
    private final Map<Integer, Intersection> intersections;
    private final Map<Integer, BoardPath> paths;
    private final List<Harbor> harbors;

    public Board(List<HexTile> tiles, List<Intersection> intersections, List<BoardPath> paths, List<Harbor> harbors) {
        this.tiles = tiles.stream()
                .collect(Collectors.toMap(HexTile::getCoordinate, t -> t));
        this.intersections = intersections.stream()
                .collect(Collectors.toMap(Intersection::getId, i -> i));
        this.paths = paths.stream()
                .collect(Collectors.toMap(BoardPath::getId, p -> p));
        this.harbors = Collections.unmodifiableList(new ArrayList<>(harbors));
    }

    // tile  

    public Optional<HexTile> getTileAt(HexCoordinate coord) {
        return Optional.ofNullable(tiles.get(coord));
    }

    public Collection<HexTile> getAllTiles() {
        return Collections.unmodifiableCollection(tiles.values());
    }

    public List<HexTile> getTilesByToken(int diceRoll) {
        return tiles.values().stream()
                .filter(t -> t.getTokenNumber() == diceRoll)
                .collect(Collectors.toList());
    }

    public List<HexTile> getAdjacentTiles(Intersection intersection) {
        return intersection.getAdjacentTileCoords().stream()
                .map(tiles::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<Intersection> getIntersection(int id) {
        return Optional.ofNullable(intersections.get(id));
    }

    public Collection<Intersection> getAllIntersections() {
        return Collections.unmodifiableCollection(intersections.values());
    }

    public List<Intersection> getIntersectionsAdjacentTo(HexCoordinate tileCoord) {
        return intersections.values().stream()
                .filter(i -> i.getAdjacentTileCoords().contains(tileCoord))
                .collect(Collectors.toList());
    }

    public List<Intersection> getNeighborIntersections(int intersectionId) {
        return getIntersection(intersectionId)
                .map(i -> i.getAdjacentIntersectionIds().stream()
                        .map(intersections::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public Optional<BoardPath> getPath(int id) {
        return Optional.ofNullable(paths.get(id));
    }

    public Collection<BoardPath> getAllPaths() {
        return Collections.unmodifiableCollection(paths.values());
    }

    public List<BoardPath> getPathsAt(int intersectionId) {
        return getIntersection(intersectionId)
                .map(i -> i.getAdjacentPathIds().stream()
                        .map(paths::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public Optional<BoardPath> getPathBetween(int intersectionIdA, int intersectionIdB) {
        return paths.values().stream()
                .filter(p -> p.connectsTo(intersectionIdA) && p.connectsTo(intersectionIdB))
                .findFirst();
    }


    public List<Harbor> getAllHarbors() {
        return harbors;
    }

    public int getBestTradeRatio(ResourceType resource, Set<Integer> playerIntersectionIds) {
        int best = 4;
        for (Harbor harbor : harbors) {
            boolean hasAccess = playerIntersectionIds.contains(harbor.getIntersectionIdA())
                    || playerIntersectionIds.contains(harbor.getIntersectionIdB());
            if (hasAccess) {
                int ratio = harbor.getTradeRatioFor(resource);
                if (ratio < best) best = ratio;
            }
        }
        return best;
    }

    public List<Intersection> getIntersectionsAroundTile(HexCoordinate tileCoord) {
        return getIntersectionsAdjacentTo(tileCoord);
    }
}
