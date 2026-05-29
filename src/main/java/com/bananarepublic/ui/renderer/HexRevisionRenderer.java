package com.bananarepublic.ui.renderer;

import com.bananarepublic.BananaRepublic;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.board.*;

import java.util.*;

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
        text.setFill(Color.BLACK);

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
    //  Coordinate utilities
    // ──────────────────────────────────────────────────────────────

    private PixelPoint toCenterPoint(HexCoordinate coord) {
        double centerX = BOARD_ANCHOR_X + HEX_SPACING_X * (coord.getQ() + coord.getR() / 2.0);
        double centerY = BOARD_ANCHOR_Y + HEX_SPACING_Y * coord.getR();
        return new PixelPoint(centerX, centerY);
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

    private record PixelPoint(double x, double y) {}
}
