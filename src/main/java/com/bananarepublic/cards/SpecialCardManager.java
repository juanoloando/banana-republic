package com.bananarepublic.cards;

import java.util.HashMap;
import java.util.Map;

public class SpecialCardManager {
    private final Map<CardType, Integer> specialCardOwners = new HashMap<>();

    public Integer getOwner(CardType type) {
        return specialCardOwners.get(type);
    }

    public void setOwner(CardType type, Integer playerIndex) {
        if (playerIndex == null) {
            specialCardOwners.remove(type);
            return;
        }
        specialCardOwners.put(type, playerIndex);
    }

    public void clearOwner(CardType type) {
        specialCardOwners.remove(type);
    }
}

