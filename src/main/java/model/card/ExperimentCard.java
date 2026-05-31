package model.card;

import model.player.Player;


public interface ExperimentCard {

    String getCardName();
    String getDescription();
    void applyEffect(GameStateContext state, Player player);
    CardCategory getCategory();

    default boolean isRevealedWhenPlayed() {
        return true;
    }
}
