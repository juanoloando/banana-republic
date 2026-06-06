package com.bananarepublic.trade;

import model.resource.ResourceType;

public enum TradeResource {
    BANANA,
    WOOD,
    WHEAT,
    ORE,
    BRICK;

    public static TradeResource fromModel(ResourceType resourceType) {
        if (resourceType == null) {
            return null;
        }

        return switch (resourceType) {
            case BANANA -> BANANA;
            case WOOD -> WOOD;
            case WHEAT -> WHEAT;
            case ORE -> ORE;
            case BRICK -> BRICK;
        };
    }

    public ResourceType toModel() {
        return switch (this) {
            case BANANA -> ResourceType.BANANA;
            case WOOD -> ResourceType.WOOD;
            case WHEAT -> ResourceType.WHEAT;
            case ORE -> ResourceType.ORE;
            case BRICK -> ResourceType.BRICK;
        };
    }

    public String displayName() {
        return switch (this) {
            case BANANA -> "Banana";
            case WOOD -> "Wood";
            case WHEAT -> "Wheat";
            case ORE -> "Ore";
            case BRICK -> "Brick";
        };
    }
}
