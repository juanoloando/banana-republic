package model.board;

import model.resource.ResourceType;
import java.util.Optional;


public final class Harbor {

    private final HarborType type;
    private final int intersectionIdA;
    private final int intersectionIdB;

    public Harbor(HarborType type, int intersectionIdA, int intersectionIdB) {
        this.type = type;
        this.intersectionIdA = intersectionIdA;
        this.intersectionIdB = intersectionIdB;
    }

    public HarborType getType() { return type; }
    public int getIntersectionIdA() { return intersectionIdA; }
    public int getIntersectionIdB() { return intersectionIdB; }

    public int getTradeRatioFor(ResourceType resource) {
        if (type.isGeneral()) return type.getTradeRatio();
        return type.getSpecialResource()
                .filter(r -> r == resource)
                .map(r -> type.getTradeRatio())
                .orElse(4); // default bank rate
    }

    public boolean servesIntersection(int intersectionId) {
        return intersectionIdA == intersectionId || intersectionIdB == intersectionId;
    }

    public Optional<ResourceType> getSpecialResource() {
        return type.getSpecialResource();
    }

    @Override
    public String toString() {
        return "Harbor{type=" + type + ", intersections=[" + intersectionIdA + "," + intersectionIdB + "]}";
    }
}
