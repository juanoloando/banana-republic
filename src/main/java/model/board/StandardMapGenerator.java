package model.board;

import model.exception.InvalidBoardException;

import java.util.*;

/*
 * map dri spek:
 *  - 19 hex tiles 
 *  - Forest x4, Hill x3, Field x4, Mountain x3, BananaGrove x4, Desert x1
 *  - Tokens: 2x1, 3x2, 4x2, 5x2, 6x2, 8x2, 9x2, 10x2, 11x2, 12x1
 *  - 9 harbors
 *
 *  layout axial coordinates ""
 *  - tengah: (0,0)
 *  - ring 1: 6 tiles
 *  - ring 2: 12 tiles (outer)
 */
public class StandardMapGenerator implements MapGenerator {

    @Override
    public Board generateBoard() {
        List<HexTile> tiles = createTiles();
        IntersectionPathBuilder builder = new IntersectionPathBuilder(tiles);
        List<Intersection> intersections = builder.buildIntersections();
        List<BoardPath> paths = builder.buildPaths();
        List<Harbor> harbors = createHarbors(intersections);
        Board board = new Board(tiles, intersections, paths, harbors);
        validateBoard(board);
        return board;
    }

    @Override
    public String getGeneratorName() {
        return "Standard Banana Republic Map";
    }

    private List<HexTile> createTiles() {
        int[][] coords = {
            // tengah
            {0, 0},
            // ring 1
            {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1},
            // ring 2
            {2, 0}, {2, -1}, {2, -2}, {1, -2}, {0, -2}, {-1, -1},
            {-2, 0}, {-2, 1}, {-2, 2}, {-1, 2}, {0, 2}, {1, 1}
        };

        TerrainType[] terrains = {
            TerrainType.DESERT,
            TerrainType.FOREST, TerrainType.HILL, TerrainType.FIELD,
            TerrainType.MOUNTAIN, TerrainType.BANANA_GROVE, TerrainType.FOREST,
            TerrainType.FIELD, TerrainType.MOUNTAIN, TerrainType.FOREST,
            TerrainType.BANANA_GROVE, TerrainType.HILL, TerrainType.FIELD,
            TerrainType.BANANA_GROVE, TerrainType.FOREST, TerrainType.HILL,
            TerrainType.FIELD, TerrainType.MOUNTAIN, TerrainType.BANANA_GROVE
        };

        int[] tokens = {
            0,  // desert
            5, 2, 6, 3, 8, 10,
            9, 12, 11, 4, 8, 10,
            9, 4, 5, 6, 3, 11
        };

        List<HexTile> tiles = new ArrayList<>();
        for (int i = 0; i < coords.length; i++) {
            HexCoordinate coord = new HexCoordinate(coords[i][0], coords[i][1]);
            tiles.add(new HexTile(coord, terrains[i], tokens[i]));
        }
        return tiles;
    }

    private List<Harbor> createHarbors(List<Intersection> intersections) {
        List<Harbor> harbors = new ArrayList<>();
        harbors.add(new Harbor(HarborType.GENERAL, 0,  1));
        harbors.add(new Harbor(HarborType.WOOD,3,  7));
        harbors.add(new Harbor(HarborType.BANANA,12, 18));
        harbors.add(new Harbor(HarborType.GENERAL, 22, 27));
        harbors.add(new Harbor(HarborType.ORE, 33, 38));
        harbors.add(new Harbor(HarborType.GENERAL, 42, 47));
        harbors.add(new Harbor(HarborType.WHEAT, 48, 49));
        harbors.add(new Harbor(HarborType.BRICK, 50, 51));
        harbors.add(new Harbor(HarborType.GENERAL, 52, 53));

        for (Harbor harbor : harbors) {
            intersections.stream()
                .filter(i -> i.getId() == harbor.getIntersectionIdA()
                          || i.getId() == harbor.getIntersectionIdB())
                .forEach(i -> i.setHarbor(harbor));
        }
        return harbors;
    }

    private void validateBoard(Board board) {
        long tileCount = board.getAllTiles().size();
        if (tileCount != 19) {
            throw new InvalidBoardException("Board must have exactly 19 tiles, got: " + tileCount);
        }
        Map<Integer, Long> tokenCounts = new HashMap<>();
        for (HexTile tile : board.getAllTiles()) {
            if (tile.isProductive()) {
                tokenCounts.merge(tile.getTokenNumber(), 1L, Long::sum);
            }
        }
        if (tokenCounts.getOrDefault(7, 0L) > 0) {
            throw new InvalidBoardException("Token 7 must not exist on any tile");
        }
    }
}
