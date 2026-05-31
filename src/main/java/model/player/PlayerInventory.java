package model.player;

import model.card.ExperimentCard;
import model.exception.InsufficientResourceException;
import model.resource.ResourceType;

import java.util.*;
import java.util.stream.Collectors;


public class PlayerInventory {

    private final EnumMap<ResourceType, Integer> resources;
    private final List<ExperimentCard> cards;
    private int knightCardsPlayed;

    public PlayerInventory() {
        this.resources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
        this.cards = new ArrayList<>();
        this.knightCardsPlayed = 0;
    }

    // resources 

    public int getResourceCount(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    public int getTotalResourceCount() {
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addResource(ResourceType type, int amount) {
        assert amount > 0 : "Amount to add must be positive";
        resources.merge(type, amount, Integer::sum);
    }

    public void removeResource(ResourceType type, int amount) {
        assert amount > 0 : "Amount to remove must be positive";
        int current = getResourceCount(type);
        if (current < amount) {
            throw new InsufficientResourceException(type, amount, current);
        }
        resources.put(type, current - amount);
    }

    public boolean canAfford(Map<? extends ResourceType, ? extends Integer> cost) {
        for (ResourceType type : ResourceType.values()) {
            Integer requiredValue = cost.get(type);
            int required = requiredValue == null ? 0 : requiredValue;
            if (required > 0 && getResourceCount(type) < required) return false;
        }
        return true;
    }

    public void pay(Map<ResourceType, Integer> cost) {
        assert cost != null && !cost.isEmpty() : "Cost map must not be null or empty";
        assert canAfford(cost) : "Cannot pay — insufficient resources. Cost: " + cost
            + ", Available: " + getResourceSnapshot();
        cost.forEach(this::removeResource);
    }

    /** Read-only snapshot for UI display and save/load. */
    public Map<ResourceType, Integer> getResourceSnapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(resources));
    }

    // cards 

    public void addCard(ExperimentCard card) {
        cards.add(card);
    }

    public boolean removeCard(ExperimentCard card) {
        return cards.remove(card);
    }

    public List<ExperimentCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int getCardCount() {
        return cards.size();
    }

    public int getKnightCardsPlayed() {
        return knightCardsPlayed;
    }

    public void incrementKnightCardsPlayed() {
        knightCardsPlayed++;
    }

    public List<ExperimentCard> getCardsByCategory(model.card.CardCategory category) {
        return cards.stream()
                .filter(c -> c.getCategory() == category)
                .collect(Collectors.toList());
    }
}
