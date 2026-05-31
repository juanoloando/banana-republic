package model.board;

import model.resource.ResourceType;
import java.util.Optional;


public enum HarborType {
    GENERAL(3, null),
    WOOD(2, ResourceType.WOOD),
    BRICK(2, ResourceType.BRICK),
    WHEAT(2, ResourceType.WHEAT),
    ORE(2, ResourceType.ORE),
    BANANA(2, ResourceType.BANANA);

    private final int tradeRatio;
    private final ResourceType specialResource;

    HarborType(int tradeRatio, ResourceType specialResource) {
        this.tradeRatio = tradeRatio;
        this.specialResource = specialResource;
    }

    public int getTradeRatio() { return tradeRatio; }

    public Optional<ResourceType> getSpecialResource() {
        return Optional.ofNullable(specialResource);
    }

    public boolean isGeneral() { return this == GENERAL; }
}
