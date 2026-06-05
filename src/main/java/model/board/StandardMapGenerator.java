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
        // Harbor tile coordinates match the visual HARBOR_BOX_CENTERS positions in HexRevisionRenderer
        HexCoordinate[] harborTiles = {
            new HexCoordinate(1, -2),   // GENERAL  (top)
            new HexCoordinate(-1, -1),  // WOOD     (upper-left)
            new HexCoordinate(2, -1),   // BANANA   (upper-right)
            new HexCoordinate(-2, 0),   // GENERAL  (left)
            new HexCoordinate(2, 0),    // ORE      (right)
            new HexCoordinate(-2, 1),   // GENERAL  (lower-left)
            new HexCoordinate(1, 1),    // WHEAT    (lower-right)
            new HexCoordinate(-2, 2),   // BRICK    (bottom-left)
            new HexCoordinate(0, 2),    // GENERAL  (bottom-right)
        };
        HarborType[] harborTypes = {
            HarborType.GENERAL, HarborType.WOOD,    HarborType.BANANA,
            HarborType.GENERAL, HarborType.ORE,     HarborType.GENERAL,
            HarborType.WHEAT,   HarborType.BRICK,   HarborType.GENERAL
        };

        List<Harbor> harbors = new ArrayList<>();
        for (int h = 0; h < harborTiles.length; h++) {
            HexCoordinate tileCoord = harborTiles[h];
            HarborType type = harborTypes[h];

            // Coastal intersections: adjacent to this harbor tile AND adj count < 3
            List<Intersection> coastal = new ArrayList<>();
            for (Intersection inter : intersections) {
                List<HexCoordinate> adj = inter.getAdjacentTileCoords();
                if (adj.contains(tileCoord) && adj.size() < 3) {
                    coastal.add(inter);
                }
            }
            if (coastal.size() < 2) continue;

            // Sort outermost first (adj==1 before adj==2)
            coastal.sort(Comparator.comparingInt(i -> i.getAdjacentTileCoords().size()));

            Harbor harbor = new Harbor(type, coastal.get(0).getId(), coastal.get(1).getId());
            harbors.add(harbor);
            // Mark ALL coastal intersections of this harbor tile so any building grants access
            for (Intersection inter : coastal) {
                inter.setHarbor(harbor);
            }
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
