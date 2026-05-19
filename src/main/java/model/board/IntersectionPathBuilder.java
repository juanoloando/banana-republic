package model.board;

import java.util.*;

// helper class buat build intersections and paths dari list of tiles
class IntersectionPathBuilder {

    private final List<HexTile> tiles;
    private final Map<HexCoordinate, HexTile> tileMap;

    IntersectionPathBuilder(List<HexTile> tiles) {
        this.tiles = tiles;
        this.tileMap = new HashMap<>();
        for (HexTile tile : tiles) {
            tileMap.put(tile.getCoordinate(), tile);
        }
    }

    List<Intersection> buildIntersections() {
        Map<String, Intersection> seen = new LinkedHashMap<>();
        int[] idCounter = {0};

        for (HexTile tile : tiles) {
            HexCoordinate c = tile.getCoordinate();
            List<HexCoordinate> neighbors = c.getNeighbors();
            int[][] vertexNeighborPairs = {{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}};
            for (int[] pair : vertexNeighborPairs) {
                HexCoordinate n1 = neighbors.get(pair[0]);
                HexCoordinate n2 = neighbors.get(pair[1]);
                String key = canonicalKey(c, n1, n2);
                if (!seen.containsKey(key)) {
                    List<HexCoordinate> adjCoords = new ArrayList<>();
                    adjCoords.add(c);
                    if (tileMap.containsKey(n1)) adjCoords.add(n1);
                    if (tileMap.containsKey(n2)) adjCoords.add(n2);
                    Intersection intersection = new Intersection(
                            idCounter[0]++, adjCoords,
                            new ArrayList<>(), new ArrayList<>());
                    seen.put(key, intersection);
                }
            }
        }
        return new ArrayList<>(seen.values());
    }

    List<BoardPath> buildPaths() {
        return new ArrayList<>();
    }

    private String canonicalKey(HexCoordinate a, HexCoordinate b, HexCoordinate c) {
        List<String> parts = Arrays.asList(a.toString(), b.toString(), c.toString());
        Collections.sort(parts);
        return String.join("|", parts);
    }
}
