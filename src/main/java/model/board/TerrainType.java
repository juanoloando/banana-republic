package model.board;

import model.resource.ResourceType;
import java.util.Optional;


public enum TerrainType {

    FOREST(ResourceType.WOOD, 4),
    HILL(ResourceType.BRICK, 3),
    FIELD(ResourceType.WHEAT, 4),
    MOUNTAIN(ResourceType.ORE, 3),
    BANANA_GROVE(ResourceType.BANANA, 4),
    DESERT(null, 1);

    private final ResourceType producedResource;
    private final int tileCount;

    TerrainType(ResourceType producedResource, int tileCount) {
        this.producedResource = producedResource;
        this.tileCount = tileCount;
    }

    public Optional<ResourceType> getProducedResource() {
        return Optional.ofNullable(producedResource);
    }

    // produce resource ato ngga
    public boolean isProductive() {
        return producedResource != null;
    }

    public int getTileCount() {
        return tileCount;
    }
}
