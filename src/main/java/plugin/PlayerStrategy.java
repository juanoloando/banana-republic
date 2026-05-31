package plugin;

import engine.GameState;
import model.player.Player;

public interface PlayerStrategy {

    // method yang dipanggil sama game engine pas giliran bot
    void takeTurn(GameState state, Player self);

    // nama strategi buat didisplay di UI
    String getStrategyName();
}
