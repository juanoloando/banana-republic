package model.resource;

import model.exception.InsufficientResourceException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


public class ResourceBank implements Bank {

    public static final int INITIAL_COUNT = 19;

    private final EnumMap<ResourceType, Integer> stock;

    public ResourceBank() {
        stock = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            stock.put(type, INITIAL_COUNT);
        }
    }

    /** Constructor for loading from a saved game. */
    public ResourceBank(Map<ResourceType, Integer> savedStock) {
        stock = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            stock.put(type, savedStock.getOrDefault(type, 0));
        }
    }

    @Override
    public int getAvailable(ResourceType type) {
        return stock.getOrDefault(type, 0);
    }

    @Override
    public boolean hasResource(ResourceType type, int amount) {
        return getAvailable(type) >= amount;
    }

    @Override
    public void takeResource(ResourceType type, int amount) {
        assert amount > 0 : "Amount to take must be positive, got: " + amount;
        int available = getAvailable(type);
        if (available < amount) {
            throw new InsufficientResourceException(type, amount, available);
        }
        stock.put(type, available - amount);
    }

    @Override
    public void returnResource(ResourceType type, int amount) {
        assert amount > 0 : "Amount to return must be positive, got: " + amount;
        int current = getAvailable(type);
        assert current + amount <= INITIAL_COUNT
            : "Returning " + amount + " " + type + " would exceed bank capacity of " + INITIAL_COUNT
              + " (current=" + current + ")";
        stock.put(type, current + amount);
    }

    /** Read-only snapshot of current stock — useful for UI display and saving. */
    public Map<ResourceType, Integer> getStockSnapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(stock));
    }
}
