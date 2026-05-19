package model.building;

import model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class BuildCost {

    public static final BuildCost PIPE = new BuildCost(Map.of(
            ResourceType.WOOD, 1,
            ResourceType.BRICK, 1
    ));

    public static final BuildCost SETTLEMENT = new BuildCost(Map.of(
            ResourceType.WOOD, 1,
            ResourceType.BRICK, 1,
            ResourceType.BANANA, 1,
            ResourceType.WHEAT, 1
    ));

    public static final BuildCost LABORATORY = new BuildCost(Map.of(
            ResourceType.ORE, 3,
            ResourceType.WHEAT, 2
    ));

    public static final BuildCost EXPERIMENT_CARD = new BuildCost(Map.of(
            ResourceType.ORE, 1,
            ResourceType.BANANA, 1,
            ResourceType.WHEAT, 1
    ));

    private final EnumMap<ResourceType, Integer> costs;

    public BuildCost(Map<ResourceType, Integer> costs) {
        this.costs = new EnumMap<>(costs);
    }

    public int getCost(ResourceType type) {
        return costs.getOrDefault(type, 0);
    }

    public Map<ResourceType, Integer> getAllCosts() {
        return Collections.unmodifiableMap(costs);
    }

    public boolean canAfford(Map<? extends ResourceType, ? extends Integer> available) {
        for (ResourceType type : ResourceType.values()) {
            int required = getCost(type);
            if (required == 0) continue;
            Integer have = available.get(type);
            if (have == null) have = 0;
            if (have < required) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BuildCost" + costs;
    }
}
