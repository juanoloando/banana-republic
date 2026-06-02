package com.bananarepublic.view.renderer;

import com.bananarepublic.BananaRepublic;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.board.*;

import java.util.*;
import java.util.function.Consumer;

public class HexRevisionRenderer {

    // Tile render dimensions (calibrated for game_revision assets at 1920x1080 layout)
    private static final double TILE_RENDER_WIDTH   = 120.0;
    private static final double TILE_RENDER_HEIGHT  = 113.0;
    private static final double HEX_SPACING_X       = 130.0;
    private static final double HEX_SPACING_Y       = 97.0;

    // Board center anchor in boardPane local coordinates (calibrate visually if needed)
    private static final double BOARD_ANCHOR_X = 671.0;
    private static final double BOARD_ANCHOR_Y = 578.0;

    private static final double TOKEN_OFFSET_Y = 40.0;
    private static final double TOKEN_OFFSET_X = 4.0;

    private static final String ROBBER_IMAGE_PATH =
            "/com/bananarepublic/images/game_revision/purple_nimon.png";

    // Tag used on nodes added by this renderer so they can be selectively removed
    private static final String RENDERER_TAG = "hex-renderer";

    public void render(Pane boardPane,
                       Collection<HexTile> tiles,
                       HexCoordinate robberPosition,
                       Board board) {
        // Remove only renderer-owned nodes — preserves FXML children (labels, buttons, etc.)
        boardPane.getChildren().removeIf(n -> RENDERER_TAG.equals(n.getUserData()));

        Set<HexCoordinate> boardCoords = new HashSet<>();
        for (HexTile tile : tiles) {
            boardCoords.add(tile.getCoordinate());
        }

        for (HexTile tile : tiles) {
            Group node = createTileNode(tile, robberPosition);
            node.setUserData(RENDERER_TAG);
            boardPane.getChildren().add(node);
        }

        renderHarbors(boardPane, board, boardCoords);
    }

    private Group createTileNode(HexTile tile, HexCoordinate robberPos) {
        PixelPoint center = toCenterPoint(tile.getCoordinate());
        Group group = new Group();

        ImageView imgView = new ImageView(loadTerrainImage(tile.getTerrainType()));
        imgView.setFitWidth(TILE_RENDER_WIDTH);
        imgView.setFitHeight(TILE_RENDER_HEIGHT);
        imgView.setPreserveRatio(false);
        imgView.setSmooth(true);
        imgView.setLayoutX(center.x() - TILE_RENDER_WIDTH / 2.0);
        imgView.setLayoutY(center.y() - TILE_RENDER_HEIGHT / 2.0);
        group.getChildren().add(imgView);

        if (tile.isProductive() && tile.getTokenNumber() != 0) {
            group.getChildren().add(createTokenText(tile, center));
        }

        if (tile.getCoordinate().equals(robberPos)) {
            group.getChildren().add(createRobberImage(center));
        }

        return group;
    }

    private Text createTokenText(HexTile tile, PixelPoint center) {
        Text text = new Text(String.valueOf(tile.getTokenNumber()));
        text.setFont(Font.font("Arcade Normal", FontWeight.BOLD, 10));
        text.setFill(tile.isHighProbability()
                ? Color.rgb(188, 40, 40)
                : Color.rgb(45, 35, 25));

        // Measure and center
        double approxWidth = tile.getTokenNumber() >= 10 ? 28 : 16;
        text.setLayoutX(center.x() - approxWidth / 2.0 + TOKEN_OFFSET_X);
        text.setLayoutY(center.y() + TOKEN_OFFSET_Y);
        return text;
    }

    private ImageView createRobberImage(PixelPoint center) {
        Image img = new Image(BananaRepublic.class.getResource(ROBBER_IMAGE_PATH).toExternalForm());
        ImageView view = new ImageView(img);
        view.setFitWidth(46);
        view.setFitHeight(46);
        view.setLayoutX(center.x() - 23);
        view.setLayoutY(center.y() - 23);
        return view;
    }

    // ──────────────────────────────────────────────────────────────
    //  Harbor rendering — dynamic, driven by Board.getAllHarbors()
    // ──────────────────────────────────────────────────────────────

    private void renderHarbors(Pane boardPane, Board board, Set<HexCoordinate> boardCoords) {
        List<Harbor> harbors = new ArrayList<>(board.getAllHarbors());
        for (int i = 0; i < Math.min(harbors.size(), HARBOR_BOX_CENTERS.length); i++) {
            Group harborNode = createHarborNode(harbors.get(i), i);
            harborNode.setUserData(RENDERER_TAG);
            boardPane.getChildren().add(harborNode);
        }
    }

    // Harbor font size — edit here
    private static final double HARBOR_FONT_SIZE = 10.0;

    // 9 pre-drawn harbor box centers from Main Game.png (boardPane coordinates).
    // Order matches StandardMapGenerator.getAllHarbors() (index 0..8).
    // Edit individual entries to fine-tune label position inside each box.
    private static final double[][] HARBOR_BOX_CENTERS = {
        { 671.2,  280   },   // [0]
        { 295.8,  415.4 },   // [1]
        { 1042.4, 415.4 },   // [2]
        { 202.9,  585.1 },   // [3]
        { 1131.6, 586.4 },   // [4]
        { 313.2,  783.2 },   // [5]
        { 1026,   783.2 },   // [6]
        { 521,    882   },   // [7]
        { 831.3,  878.2 },   // [8]
    };

    private Group createHarborNode(Harbor harbor, int index) {
        double[] box = HARBOR_BOX_CENTERS[index];
        Group group = new Group();

        String label = formatHarborLabel(harbor);
        Text text = new Text(label);
        text.setFont(Font.font("Arcade Normal", FontWeight.BOLD, HARBOR_FONT_SIZE));
        text.setFill(Color.TRANSPARENT);

        // Approximate horizontal centering (~9px per char at 16pt)
        double approxWidth = label.length() * 9.0;
        text.setLayoutX(box[0] - approxWidth / 2.0);
        text.setLayoutY(box[1] + HARBOR_FONT_SIZE / 2.0);
        group.getChildren().add(text);

        return group;
    }

    private String formatHarborLabel(Harbor harbor) {
        if (harbor.getType() == HarborType.GENERAL) return "3:1";
        return switch (harbor.getType()) {
            case BANANA -> "2:1";
            case WOOD   -> "2:1";
            case BRICK  -> "2:1";
            case WHEAT  -> "2:1";
            case ORE    -> "2:1";
            default     -> "2:1";
        };
    }

    // ──────────────────────────────────────────────────────────────
    //  Build-mode indicators
    // ──────────────────────────────────────────────────────────────

    private static final String BUILD_TAG = "build-indicator";
    private static final double BUILD_CIRCLE_RADIUS = 11.0;  // building circle radius
    private static final double PIPE_CIRCLE_RADIUS  =  8.0;  // pipe circle radius (smaller)

    /**
     * Renders build-mode circles:
     *   RED    = can place outpost (settlement)
     *   GOLD   = can upgrade to lab
     *   BLUE   = can place pipe, positioned at midpoint between two adjacent intersection circles
     */
    /**
     * Renders build-mode circles into a dedicated circlePane (separate from boardPane).
     * circlePane must be layered below pipePane and buildingPane in the parent container.
     */
    public void renderBuildableLocations(Pane circlePane,
                                          Set<Integer> settlementIds,
                                          Set<Integer> labIds,
                                          Set<Integer> pipeIds,
                                          Board board,
                                          java.util.function.BiConsumer<Integer, Boolean> onIntersection,
                                          Consumer<Integer> onPath) {
        clearBuildableLocations(circlePane);
        Map<Integer, PixelPoint> intPos = computeIntersectionPositions(board);

        for (int id : settlementIds) {
            PixelPoint p = intPos.get(id);
            if (p == null) continue;
            addCircle(circlePane, p, BUILD_CIRCLE_RADIUS, Color.RED,       () -> onIntersection.accept(id, false));
        }
        for (int id : labIds) {
            PixelPoint p = intPos.get(id);
            if (p == null) continue;
            addCircle(circlePane, p, BUILD_CIRCLE_RADIUS, Color.GOLD,      () -> onIntersection.accept(id, true));
        }
        for (int pathId : pipeIds) {
            board.getPath(pathId).ifPresent(path -> {
                PixelPoint a = intPos.get(path.getIntersectionIdA());
                PixelPoint b = intPos.get(path.getIntersectionIdB());
                if (a == null || b == null) return;
                PixelPoint mid = new PixelPoint((a.x() + b.x()) / 2.0, (a.y() + b.y()) / 2.0);
                addCircle(circlePane, mid, PIPE_CIRCLE_RADIUS, Color.DODGERBLUE, () -> onPath.accept(pathId));
            });
        }
    }

    /** Removes all build-mode circles from circlePane. */
    public void clearBuildableLocations(Pane circlePane) {
        circlePane.getChildren().removeIf(n -> BUILD_TAG.equals(n.getUserData()));
    }

    private void addCircle(Pane boardPane, PixelPoint pos, double radius, Color fill, Runnable onClick) {
        Circle circle = new Circle(pos.x(), pos.y(), radius);
        circle.setFill(fill);
        circle.setOpacity(0.80);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        circle.setCursor(Cursor.HAND);
        circle.setUserData(BUILD_TAG);
        circle.setOnMouseClicked(e -> { e.consume(); onClick.run(); });
        boardPane.getChildren().add(circle);
    }

    // Half-spacing constants for vertex calculations
    private static final double HEX_HALF_W      = HEX_SPACING_X / 2.0;        // 65
    private static final double HEX_THIRD_H     = HEX_SPACING_Y / 3.0;        // ≈32.33
    private static final double HEX_TWO_THIRD_H = HEX_SPACING_Y * 2.0 / 3.0; // ≈64.67

    /**
     * Returns the 6 vertex positions of a hex tile centered at p.
     * Indices 0-5: Top, TR, BR, Bottom, BL, TL (clockwise).
     */
    private PixelPoint[] tileVertices(PixelPoint p) {
        double cx = p.x(), cy = p.y();
        return new PixelPoint[]{
            new PixelPoint(cx,              cy - HEX_TWO_THIRD_H),
            new PixelPoint(cx + HEX_HALF_W, cy - HEX_THIRD_H),
            new PixelPoint(cx + HEX_HALF_W, cy + HEX_THIRD_H),
            new PixelPoint(cx,              cy + HEX_TWO_THIRD_H),
            new PixelPoint(cx - HEX_HALF_W, cy + HEX_THIRD_H),
            new PixelPoint(cx - HEX_HALF_W, cy - HEX_THIRD_H),
        };
    }

    private static double dist2(PixelPoint a, PixelPoint b) {
        double dx = a.x() - b.x(), dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    /**
     * Computes pixel positions for all intersections.
     *
     * - 3 adjacent tiles → centroid of the 3 tile centres (always correct for interior vertices)
     * - 2 adjacent tiles → find the two vertices shared by both tiles, pick the one that points
     *                       outward from the board centre (the true perimeter vertex)
     * - 1 adjacent tile  → skipped (extreme-corner intersections; generally not valid build spots)
     */
    public Map<Integer, PixelPoint> computeIntersectionPositions(Board board) {
        PixelPoint boardCenter = new PixelPoint(BOARD_ANCHOR_X, BOARD_ANCHOR_Y);
        Map<Integer, PixelPoint> result = new HashMap<>();

        // Board tile coord set — needed for adj==1 virtual-tile calculation
        Set<HexCoordinate> boardCoords = new HashSet<>();
        for (var tile : board.getAllTiles()) boardCoords.add(tile.getCoordinate());

        // ── adj==3 and adj==2 (single pass) ─────────────────────────────
        Map<HexCoordinate, List<Integer>> oneTileGroups = new HashMap<>();

        for (Intersection intersection : board.getAllIntersections()) {
            List<HexCoordinate> adj = intersection.getAdjacentTileCoords();
            PixelPoint pos = null;

            if (adj.size() == 3) {
                double x = 0, y = 0;
                for (HexCoordinate c : adj) { PixelPoint p = toCenterPoint(c); x += p.x(); y += p.y(); }
                pos = new PixelPoint(x / 3.0, y / 3.0);

            } else if (adj.size() == 2) {
                PixelPoint[] va = tileVertices(toCenterPoint(adj.get(0)));
                PixelPoint[] vb = tileVertices(toCenterPoint(adj.get(1)));
                List<PixelPoint> shared = new ArrayList<>();
                for (PixelPoint pa : va) {
                    for (PixelPoint pb : vb) {
                        if (dist2(pa, pb) < 1.0) { shared.add(pa); break; }
                    }
                }
                if (shared.size() >= 2) {
                    PixelPoint mid = new PixelPoint(
                        (toCenterPoint(adj.get(0)).x() + toCenterPoint(adj.get(1)).x()) / 2.0,
                        (toCenterPoint(adj.get(0)).y() + toCenterPoint(adj.get(1)).y()) / 2.0);
                    double dx = mid.x() - boardCenter.x(), dy = mid.y() - boardCenter.y();
                    double d0 = (shared.get(0).x()-mid.x())*dx + (shared.get(0).y()-mid.y())*dy;
                    double d1 = (shared.get(1).x()-mid.x())*dx + (shared.get(1).y()-mid.y())*dy;
                    pos = d0 > d1 ? shared.get(0) : shared.get(1);
                } else if (shared.size() == 1) {
                    pos = shared.get(0);
                }
                if (pos == null) {
                    double x = 0, y = 0;
                    for (HexCoordinate c : adj) { PixelPoint p = toCenterPoint(c); x += p.x(); y += p.y(); }
                    pos = new PixelPoint(x / 2.0, y / 2.0);
                }

            } else if (adj.size() == 1) {
                // Defer: handle after collecting all adj==1 per tile
                oneTileGroups.computeIfAbsent(adj.get(0), k -> new ArrayList<>())
                             .add(intersection.getId());
                continue;
            } else {
                continue;
            }
            result.put(intersection.getId(), pos);
        }

        // ── adj==1: assign each correct vertex via centroid with virtual tiles ───
        // IntersectionPathBuilder iterates vertex pairs {0,1},{1,2},...,{5,0} in order.
        // Lower intersection ID → earlier vertex pair index → assign in sorted order.
        int[][] vpairs = {{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}};
        for (var entry : oneTileGroups.entrySet()) {
            HexCoordinate tCoord  = entry.getKey();
            List<Integer>  ids    = new ArrayList<>(entry.getValue());
            Collections.sort(ids); // sort by ID: lower = earlier vertex pair

            List<HexCoordinate> neighbors = tCoord.getNeighbors();
            List<PixelPoint> positions = new ArrayList<>();

            // Collect vertex positions where BOTH neighbors are outside the board
            for (int[] pair : vpairs) {
                HexCoordinate n1 = neighbors.get(pair[0]);
                HexCoordinate n2 = neighbors.get(pair[1]);
                if (!boardCoords.contains(n1) && !boardCoords.contains(n2)) {
                    // Use centroid including virtual tiles (outside-board tiles still have coords)
                    PixelPoint p  = toCenterPoint(tCoord);
                    PixelPoint v1 = toCenterPoint(n1);
                    PixelPoint v2 = toCenterPoint(n2);
                    positions.add(new PixelPoint(
                        (p.x() + v1.x() + v2.x()) / 3.0,
                        (p.y() + v1.y() + v2.y()) / 3.0));
                }
            }

            for (int i = 0; i < Math.min(ids.size(), positions.size()); i++) {
                result.put(ids.get(i), positions.get(i));
            }
        }

        return result;
    }

    // ──────────────────────────────────────────────────────────────
    //  Coordinate utilities
    // ──────────────────────────────────────────────────────────────

    private PixelPoint toCenterPoint(HexCoordinate coord) {
        double centerX = BOARD_ANCHOR_X + HEX_SPACING_X * (coord.getQ() + coord.getR() / 2.0);
        double centerY = BOARD_ANCHOR_Y + HEX_SPACING_Y * coord.getR();
        return new PixelPoint(centerX, centerY);
    }

    /** Public accessor for tile center point — used by GameRevisionController for robber move mode. */
    public PixelPoint computeTileCenterPoint(HexCoordinate coord) {
        return toCenterPoint(coord);
    }

    private Image loadTerrainImage(TerrainType terrain) {
        String path = switch (terrain) {
            case FOREST      -> "/com/bananarepublic/images/game_revision/hex_forest.png";
            case HILL        -> "/com/bananarepublic/images/game_revision/hex_hill.png";
            case FIELD       -> "/com/bananarepublic/images/game_revision/hex_field.png";
            case MOUNTAIN    -> "/com/bananarepublic/images/game_revision/hex_mine.png";
            case BANANA_GROVE -> "/com/bananarepublic/images/game_revision/hex_banana.png";
            case DESERT      -> "/com/bananarepublic/images/game_revision/hex_desert.png";
        };
        return new Image(BananaRepublic.class.getResource(path).toExternalForm());
    }

    public record PixelPoint(double x, double y) {}
}
