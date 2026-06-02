package com.bananarepublic.view.card;

import model.card.CardType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class CardDefinitions {
    private static final String EXPERIMENT_TEMPLATE = "/com/bananarepublic/images/usecard/experiment_template.png";
    private static final String SPECIAL_TEMPLATE = "/com/bananarepublic/images/usecard/special_template.png";

    private static final Map<CardType, CardDefinition> DEFINITIONS;

    static {
        EnumMap<CardType, CardDefinition> map = new EnumMap<>(CardType.class);
        map.put(CardType.KNIGHT, new CardDefinition(
                CardType.KNIGHT, CardCategory.EXPERIMENT, "Knight Card",
                PlayabilityType.PLAYABLE, EXPERIMENT_TEMPLATE, false));
        map.put(CardType.ROAD_BUILDING, new CardDefinition(
                CardType.ROAD_BUILDING, CardCategory.EXPERIMENT, "Road Building",
                PlayabilityType.PLAYABLE, EXPERIMENT_TEMPLATE, false));
        map.put(CardType.MONOPOLY, new CardDefinition(
                CardType.MONOPOLY, CardCategory.EXPERIMENT, "Monopoly",
                PlayabilityType.PLAYABLE, EXPERIMENT_TEMPLATE, false));
        map.put(CardType.VICTORY_POINT, new CardDefinition(
                CardType.VICTORY_POINT, CardCategory.EXPERIMENT, "Victory Point",
                PlayabilityType.REVEAL_ONLY, EXPERIMENT_TEMPLATE, true));
        map.put(CardType.LONGEST_ROAD, new CardDefinition(
                CardType.LONGEST_ROAD, CardCategory.SPECIAL, "Longest Road",
                PlayabilityType.PASSIVE, SPECIAL_TEMPLATE, false));
        map.put(CardType.LARGEST_ARMY, new CardDefinition(
                CardType.LARGEST_ARMY, CardCategory.SPECIAL, "Largest Army",
                PlayabilityType.PASSIVE, SPECIAL_TEMPLATE, false));
        DEFINITIONS = Collections.unmodifiableMap(map);
    }

    private CardDefinitions() {}

    public static CardDefinition get(CardType type) {
        return DEFINITIONS.get(type);
    }

    public static Map<CardType, CardDefinition> all() {
        return DEFINITIONS;
    }
}

