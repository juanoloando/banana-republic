package model.card;

import model.card.ExperimentCard;
import model.card.KnightCard;
import model.card.MonopolyCard;
import model.card.RoadBuildingCard;
import model.card.VictoryPointCard;

public enum CardType {
    KNIGHT,
    ROAD_BUILDING,
    MONOPOLY,
    VICTORY_POINT,
    LONGEST_ROAD,
    LARGEST_ARMY;

    public static CardType fromExperimentCard(ExperimentCard card) {
        if (card instanceof KnightCard) return KNIGHT;
        if (card instanceof RoadBuildingCard) return ROAD_BUILDING;
        if (card instanceof MonopolyCard) return MONOPOLY;
        if (card instanceof VictoryPointCard) return VICTORY_POINT;
        return null;
    }
}

