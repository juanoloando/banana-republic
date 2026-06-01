package com.bananarepublic.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerCardInventory {
    private final EnumMap<CardType, Integer> quantities = new EnumMap<>(CardType.class);
    private final Set<CardType> boughtThisTurn = EnumSet.noneOf(CardType.class);

    public PlayerCardInventory() {
        for (CardType type : CardType.values()) {
            quantities.put(type, 0);
        }
    }

    public int getQuantity(CardType type) {
        return quantities.getOrDefault(type, 0);
    }

    public void setQuantity(CardType type, int quantity) {
        quantities.put(type, Math.max(0, quantity));
    }

    public void addCard(CardType type, int amount) {
        if (amount <= 0) return;
        quantities.put(type, getQuantity(type) + amount);
    }

    public boolean removeCard(CardType type, int amount) {
        if (amount <= 0) return false;
        int current = getQuantity(type);
        if (current < amount) return false;
        quantities.put(type, current - amount);
        return true;
    }

    public boolean hasCard(CardType type) {
        return getQuantity(type) > 0;
    }

    public void markCardBoughtThisTurn(CardType type) {
        boughtThisTurn.add(type);
    }

    public boolean isBoughtThisTurn(CardType type) {
        return boughtThisTurn.contains(type);
    }

    public void clearBoughtThisTurnFlagsAtEndTurn() {
        boughtThisTurn.clear();
    }

    public boolean canUseThisTurn(CardType type) {
        return hasCard(type) && !isBoughtThisTurn(type);
    }

    public List<CardType> getOwnedExperimentCards() {
        List<CardType> result = new ArrayList<>();
        for (Map.Entry<CardType, Integer> entry : quantities.entrySet()) {
            CardDefinition definition = CardDefinitions.get(entry.getKey());
            if (definition == null) continue;
            if (definition.getCategory() == CardCategory.EXPERIMENT && entry.getValue() > 0) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Map<CardType, Integer> snapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(quantities));
    }
}

