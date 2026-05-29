package com.bananarepublic.ui.renderer;

import java.util.EnumMap;
import java.util.Map;

import model.board.TerrainType;

public final class ResourceIconMapper {
    private static final Map<TerrainType, String> TERRAIN_IMAGE_PATHS = new EnumMap<>(TerrainType.class);

    static {
        TERRAIN_IMAGE_PATHS.put(TerrainType.FOREST, "/com/bananarepublic/images/game/hex_forest.png");
        TERRAIN_IMAGE_PATHS.put(TerrainType.HILL, "/com/bananarepublic/images/game/hex_hill.png");
        TERRAIN_IMAGE_PATHS.put(TerrainType.FIELD, "/com/bananarepublic/images/game/hex_field.png");
        TERRAIN_IMAGE_PATHS.put(TerrainType.MOUNTAIN, "/com/bananarepublic/images/game/hex_mine.png");
        TERRAIN_IMAGE_PATHS.put(TerrainType.BANANA_GROVE, "/com/bananarepublic/images/game/hex_banana.png");
        TERRAIN_IMAGE_PATHS.put(TerrainType.DESERT, "/com/bananarepublic/images/game/hex_desert.png");
    }

    private ResourceIconMapper() {
    }

    public static String getTerrainImagePath(TerrainType terrainType) {
        return TERRAIN_IMAGE_PATHS.get(terrainType);
    }
}
