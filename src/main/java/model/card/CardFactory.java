package model.card;

import java.util.ArrayList;
import java.util.List;

// caller dapet kartu dari sini, reducing  public API surface 
public final class CardFactory {

    private CardFactory() {}

    public static ExperimentCard createKnight() {
        return new KnightCard();
    }

    public static ExperimentCard createRoadBuilding() {
        return new RoadBuildingCard();
    }

    public static ExperimentCard createMonopoly() {
        return new MonopolyCard();
    }

    public static ExperimentCard createVictoryPoint() {
        return new VictoryPointCard();
    }

    public static List<ExperimentCard> createStandardDeck() {
        List<ExperimentCard> deck = new ArrayList<>();
        for (int i = 0; i < 14; i++) deck.add(createKnight());
        for (int i = 0; i < 3;  i++) deck.add(createRoadBuilding());
        for (int i = 0; i < 3;  i++) deck.add(createMonopoly());
        for (int i = 0; i < 5;  i++) deck.add(createVictoryPoint());
        assert deck.size() == 25 : "Standard deck must have exactly 25 cards, got: " + deck.size();
        return deck;
    }
}
