package model.board;

import java.util.*;

// helper class buat build intersections and paths dari list of tiles
class IntersectionPathBuilder {

    private final List<HexTile> tiles;
    private final Map<HexCoordinate, HexTile> tileMap;

    // Cache populated by buildIntersections(), reused in buildPaths()
    private Map<String, Intersection> intersectionCache;

    IntersectionPathBuilder(List<HexTile> tiles) {
        this.tiles = tiles;
        this.tileMap = new HashMap<>();
        for (HexTile tile : tiles) {
            tileMap.put(tile.getCoordinate(), tile);
        }
    }

    List<Intersection> buildIntersections() {
        intersectionCache = new LinkedHashMap<>();
        int[] idCounter = {0};

        for (HexTile tile : tiles) {
            HexCoordinate c = tile.getCoordinate();
            List<HexCoordinate> neighbors = c.getNeighbors();
            int[][] vertexNeighborPairs = {{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}};
            for (int[] pair : vertexNeighborPairs) {
                HexCoordinate n1 = neighbors.get(pair[0]);
                HexCoordinate n2 = neighbors.get(pair[1]);
                String key = canonicalKey(c, n1, n2);
                if (!intersectionCache.containsKey(key)) {
                    List<HexCoordinate> adjCoords = new ArrayList<>();
                    adjCoords.add(c);
                    if (tileMap.containsKey(n1)) adjCoords.add(n1);
                    if (tileMap.containsKey(n2)) adjCoords.add(n2);
                    Intersection intersection = new Intersection(
                            idCounter[0]++, adjCoords,
                            new ArrayList<>(), new ArrayList<>());
                    intersectionCache.put(key, intersection);
                }
            }
        }
        return new ArrayList<>(intersectionCache.values());
    }

    /**
     * Builds one BoardPath per unique hex edge.
     * Each edge connects two consecutive vertices (intersections) of a tile.
     * Must be called after buildIntersections().
     */
    List<BoardPath> buildPaths() {
        if (intersectionCache == null) {
            buildIntersections(); // ensure cache is populated
        }

        int[][] vertexNeighborPairs = {{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}};
        Set<String> seenEdges = new HashSet<>();
        List<BoardPath> paths = new ArrayList<>();
        int[] idCounter = {0};

        for (HexTile tile : tiles) {
            HexCoordinate c = tile.getCoordinate();
            List<HexCoordinate> neighbors = c.getNeighbors();

            for (int i = 0; i < 6; i++) {
                // Vertex i and vertex (i+1) are connected by an edge of this tile
                int[] pairA = vertexNeighborPairs[i];
                int[] pairB = vertexNeighborPairs[(i + 1) % 6];

                String keyA = canonicalKey(c, neighbors.get(pairA[0]), neighbors.get(pairA[1]));
                String keyB = canonicalKey(c, neighbors.get(pairB[0]), neighbors.get(pairB[1]));

                Intersection intA = intersectionCache.get(keyA);
                Intersection intB = intersectionCache.get(keyB);
                if (intA == null || intB == null) continue;

                // Deduplicate: each interior edge is shared by 2 tiles
                int idMin = Math.min(intA.getId(), intB.getId());
                int idMax = Math.max(intA.getId(), intB.getId());
                String edgeKey = idMin + ":" + idMax;
                if (!seenEdges.add(edgeKey)) continue;

                paths.add(new BoardPath(idCounter[0]++, intA.getId(), intB.getId()));
            }
        }

        return paths;
    }

    private String canonicalKey(HexCoordinate a, HexCoordinate b, HexCoordinate c) {
        List<String> parts = Arrays.asList(a.toString(), b.toString(), c.toString());
        Collections.sort(parts);
        return String.join("|", parts);
    }
}
