package model.player;

import model.resource.ResourceType;

public interface Player {
    String getName();

    PlayerColor getColor(); // buat ui (maybe)

    int getResourceCount(ResourceType type);
    int getTotalResourceCount();
    void addResource(ResourceType type, int amount);
    void removeResource(ResourceType type, int amount);
    boolean canAfford(java.util.Map<? extends ResourceType, ? extends Integer> cost);

    default boolean isBot() { return false; }
}
