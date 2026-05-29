package com.bananarepublic.ui.renderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bananarepublic.BananaRepublic;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.board.HexCoordinate;
import model.board.HexTile;
import model.board.TerrainType;

public class HexRenderer {
    private static final double TILE_RENDER_WIDTH = 118.0;
    private static final double TILE_RENDER_HEIGHT = 113.0;
    private static final double HEX_CENTER_SPACING_X = 0.0;
    private static final double HEX_CENTER_SPACING_Y = 0.0;
    private static final double BOARD_ANCHOR_X = 670.0;
    private static final double BOARD_ANCHOR_Y = 313.0;
    private static final double TOKEN_OFFSET_X = 0.0;
    private static final double TOKEN_OFFSET_Y = 44.0;
    private static final double TOKEN_FONT_SIZE = 22.0;

    private static final String ROBBER_IMAGE_PATH = "/com/bananarepublic/images/game/purple_nimon.png";

    private final Map<String, Image> imageCache = new HashMap<>();

    public void render(Pane boardPane, Collection<HexTile> tiles, HexCoordinate robberCoordinate) {
        boardPane.getChildren().clear();
        if (tiles == null || tiles.isEmpty()) {
            return;
        }

        for (HexTile tile : tiles) {
            PixelPoint center = toCenterPoint(tile.getCoordinate());
            Group tileNode = createTileNode(
                    tile,
                    center.x(),
                    center.y(),
                    TILE_RENDER_WIDTH,
                    TILE_RENDER_HEIGHT,
                    robberCoordinate
            );
            boardPane.getChildren().add(tileNode);
        }
    }

    private Group createTileNode(
            HexTile tile,
            double centerX,
            double centerY,
            double tileWidth,
            double tileHeight,
            HexCoordinate robberCoordinate
    ) {
        Group group = new Group();

        TileSpriteSpec spriteSpec = getSpriteSpec(tile.getTerrainType());
        Image spriteImage = getImage(spriteSpec.path());
        if (spriteImage == null || spriteImage.isError()) {
            return group;
        }

        ImageView tileImage = new ImageView(spriteImage);
        tileImage.setViewport(new Rectangle2D(
                spriteSpec.cropX(),
                spriteSpec.cropY(),
                spriteSpec.width(),
                spriteSpec.height()
        ));
        tileImage.setSmooth(true);
        tileImage.setFitWidth(tileWidth);
        tileImage.setFitHeight(tileHeight);
        tileImage.setPreserveRatio(false);
        tileImage.setX(centerX - (tileWidth / 2.0));
        tileImage.setY(centerY - (tileHeight / 2.0));
        group.getChildren().add(tileImage);

        if (tile.isProductive() && tile.getTerrainType() != TerrainType.DESERT) {
            TokenOffset terrainOffset = getTokenOffsetForTerrain(tile.getTerrainType());
            Text tokenText = createTokenText(
                    tile.getTokenNumber(),
                    tile.isHighProbability(),
                    centerX + TOKEN_OFFSET_X + terrainOffset.x(),
                    centerY + TOKEN_OFFSET_Y + terrainOffset.y()
            );
            group.getChildren().add(tokenText);
        }

        if (robberCoordinate != null && robberCoordinate.equals(tile.getCoordinate())) {
            ImageView robberView = createRobberImage(centerX, centerY);
            if (robberView != null) {
                group.getChildren().add(robberView);
            }
        }

        return group;
    }

    private Text createTokenText(int tokenNumber, boolean highProbability, double centerX, double centerY) {
        Text text = new Text(String.valueOf(tokenNumber));
        text.setFill(highProbability ? Color.rgb(188, 40, 40) : Color.rgb(45, 35, 25));
        text.setFont(Font.font("Press Start 2P", FontWeight.BOLD, TOKEN_FONT_SIZE));

        Bounds bounds = text.getLayoutBounds();
        double textX = centerX - bounds.getMinX() - (bounds.getWidth() / 2.0);
        double textY = centerY - bounds.getMinY() - (bounds.getHeight() / 2.0);

        text.setX(textX);
        text.setY(textY);
        return text;
    }

    private ImageView createRobberImage(double centerX, double centerY) {
        Image image = getImage(ROBBER_IMAGE_PATH);
        if (image == null || image.isError()) {
            return null;
        }

        ImageView robberView = new ImageView(image);
        robberView.setFitWidth(46);
        robberView.setFitHeight(46);
        robberView.setPreserveRatio(true);
        robberView.setSmooth(true);
        robberView.setX(centerX - 23);
        robberView.setY(centerY - 26);
        return robberView;
    }

    private PixelPoint toCenterPoint(HexCoordinate coordinate) {
        int q = coordinate.getQ();
        int r = coordinate.getR();

        // Pointy-top axial layout (manual anchor, no auto-centering).
        double centerX = BOARD_ANCHOR_X + HEX_CENTER_SPACING_X * (q + (r / 2.0));
        double centerY = BOARD_ANCHOR_Y + HEX_CENTER_SPACING_Y * r;
        return new PixelPoint(centerX, centerY);
    }

    private TokenOffset getTokenOffsetForTerrain(TerrainType terrainType) {
        return switch (terrainType) {
            case HILL -> new TokenOffset(0.0, 1.0);
            case FIELD -> new TokenOffset(0.0, 1.0);
            case MOUNTAIN -> new TokenOffset(0.0, -1.0);
            case FOREST, BANANA_GROVE, DESERT -> new TokenOffset(0.0, 0.0);
        };
    }

    private Image getImage(String imagePath) {
        return imageCache.computeIfAbsent(imagePath, path -> {
            var resource = BananaRepublic.class.getResource(path);
            if (resource == null) {
                return null;
            }
            return new Image(resource.toExternalForm());
        });
    }

    private TileSpriteSpec getSpriteSpec(TerrainType terrainType) {
        return switch (terrainType) {
            case FOREST -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 6, 9, 163, 153);
            case HILL -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 15, 27, 161, 151);
            case FIELD -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 6, 10, 161, 157);
            case MOUNTAIN -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 18, 19, 163, 151);
            case BANANA_GROVE -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 7, 2, 160, 149);
            case DESERT -> new TileSpriteSpec(ResourceIconMapper.getTerrainImagePath(terrainType), 2, 7, 160, 150);
        };
    }

    private record PixelPoint(double x, double y) {}
    private record TokenOffset(double x, double y) {}

    private record TileSpriteSpec(String path, int cropX, int cropY, int width, int height) {}
}
